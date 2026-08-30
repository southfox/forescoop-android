#!/usr/bin/env swift
//
//  install-testapp.swift
//  Ventus (forescoop-android)
//
//  Bumps the build number (versionCode), builds an APK with Gradle, but
//  no install.
//
//  versionName (e.g. "1.0.1") is the human-facing release version and is
//  only ever changed by hand in app/build.gradle.kts. versionCode is the
//  build number and increments by 1 every run (1, 2, 3, ...).
//
//  Usage:
//    ./install-testapp.swift                       # bump build number, build debug APK, install
//    ./install-testapp.swift --release              # build a release APK instead of debug
//    ./install-testapp.swift --apk app/build/outputs/apk/debug/app-debug.apk   # skip build, just install
//    ./install-testapp.swift --install            # build only, and push to a device connected with USB
//    ./install-testapp.swift --no-bump               # skip the build-number bump
//

import Foundation

// MARK: - Configuration

let projectDir = FileManager.default.currentDirectoryPath
let gradlew = "\(projectDir)/gradlew"
let appBuildGradle = "\(projectDir)/app/build.gradle.kts"
let androidHome = ProcessInfo.processInfo.environment["ANDROID_HOME"]
    ?? ProcessInfo.processInfo.environment["ANDROID_SDK_ROOT"]
    ?? "/opt/homebrew/share/android-commandlinetools"
let adbPath = "\(androidHome)/platform-tools/adb"

// MARK: - Helpers

func fail(_ message: String) -> Never {
    FileHandle.standardError.write("\n❌ \(message)\n".data(using: .utf8)!)
    exit(1)
}

func step(_ message: String) {
    print("\n▶︎ \(message)")
}

@discardableResult
func run(_ launchPath: String, _ arguments: [String], cwd: String? = nil, env: [String: String]? = nil) -> Int32 {
    let process = Process()
    // Use /bin/bash to run gradlew to ensure environment and shebang are handled correctly
    if launchPath.hasSuffix("gradlew") {
        process.executableURL = URL(fileURLWithPath: "/bin/bash")
        process.arguments = [launchPath] + arguments
    } else {
        process.executableURL = URL(fileURLWithPath: launchPath)
        process.arguments = arguments
    }

    if let cwd { process.currentDirectoryURL = URL(fileURLWithPath: cwd) }

    var environment = ProcessInfo.processInfo.environment
    if let env {
        for (key, value) in env {
            environment[key] = value
        }
    }
    // Ensure we have a valid home for Android tools to avoid AndroidLocationsBuildService failures.
    // We try to use the current HOME or a temporary directory if it fails.
    if environment["ANDROID_USER_HOME"] == nil {
        let home = environment["HOME"] ?? "/tmp"
        environment["ANDROID_USER_HOME"] = "\(home)/.android"
    }
    process.environment = environment

    process.standardOutput = FileHandle.standardOutput
    process.standardError = FileHandle.standardError
    do {
        try process.run()
    } catch {
        fail("Failed to launch \(launchPath): \(error)")
    }
    process.waitUntilExit()
    return process.terminationStatus
}

func capture(_ launchPath: String, _ arguments: [String]) -> String {
    let process = Process()
    process.executableURL = URL(fileURLWithPath: launchPath)
    process.arguments = arguments
    let pipe = Pipe()
    process.standardOutput = pipe
    process.standardError = pipe
    do {
        try process.run()
    } catch {
        fail("Failed to launch \(launchPath): \(error)")
    }
    let data = pipe.fileHandleForReading.readDataToEndOfFile()
    process.waitUntilExit()
    return String(data: data, encoding: .utf8) ?? ""
}

let fileManager = FileManager.default

// MARK: - Argument parsing

var explicitAPKPath: String? = nil
var buildRelease = false
var shouldInstall = false
var shouldBump = true

let arguments = Array(CommandLine.arguments.dropFirst())
var index = 0
while index < arguments.count {
    switch arguments[index] {
    case "--apk":
        index += 1
        guard index < arguments.count else { fail("--apk requires a path") }
        explicitAPKPath = arguments[index]
    case "--release":
        buildRelease = true
    case "--install":
        shouldInstall = true
    case "--no-bump":
        shouldBump = false
    default:
        fail("Unknown argument: \(arguments[index])")
    }
    index += 1
}

guard fileManager.fileExists(atPath: gradlew) else {
    fail("Could not find gradlew at \(gradlew). Run this script from the project root.")
}

func readVersionName() -> String? {
    guard let contents = try? String(contentsOfFile: appBuildGradle, encoding: .utf8),
          let versionNameRange = contents.range(of: #"versionName\s*=\s*"([^"]*)""#, options: .regularExpression) else {
        return nil
    }
    let match = String(contents[versionNameRange])
    guard let quotedRange = match.range(of: #""([^"]*)""#, options: .regularExpression) else { return nil }
    return match[quotedRange].trimmingCharacters(in: CharacterSet(charactersIn: "\""))
}

func readVersionCode() -> Int? {
    guard let contents = try? String(contentsOfFile: appBuildGradle, encoding: .utf8),
          let versionCodeRange = contents.range(of: #"versionCode\s*=\s*(\d+)"#, options: .regularExpression) else {
        return nil
    }
    let match = String(contents[versionCodeRange])
    guard let digitsRange = match.range(of: #"\d+"#, options: .regularExpression) else { return nil }
    return Int(match[digitsRange])
}

var apkPath: String

if let explicitAPKPath {
    apkPath = explicitAPKPath
    guard fileManager.fileExists(atPath: apkPath) else { fail("No APK found at \(apkPath)") }
    step("Using existing APK at \(apkPath), skipping bump/build.")
} else {
    // 1. Bump versionCode (the build number). versionName is the human-facing
    // release version (e.g. "1.0.1") and is only ever changed by hand.
    if shouldBump {
        step("Bumping build number (versionCode)")
        guard var contents = try? String(contentsOfFile: appBuildGradle, encoding: .utf8) else {
            fail("Could not read \(appBuildGradle)")
        }
        guard let versionCodeRange = contents.range(of: #"versionCode\s*=\s*(\d+)"#, options: .regularExpression) else {
            fail("Could not find versionCode in \(appBuildGradle)")
        }
        let versionCodeMatch = String(contents[versionCodeRange])
        guard let digitsRange = versionCodeMatch.range(of: #"\d+"#, options: .regularExpression),
              let currentVersionCode = Int(versionCodeMatch[digitsRange]) else {
            fail("Could not parse versionCode from \"\(versionCodeMatch)\"")
        }
        let newVersionCode = currentVersionCode + 1
        contents.replaceSubrange(versionCodeRange, with: "versionCode = \(newVersionCode)")
        print("Build: \(currentVersionCode) -> \(newVersionCode)")

        do {
            try contents.write(toFile: appBuildGradle, atomically: true, encoding: .utf8)
        } catch {
            fail("Could not write \(appBuildGradle): \(error)")
        }
    }

    // 2. Build
    let gradleTask = buildRelease ? ":app:assembleRelease" : ":app:assembleDebug"
    step("Building APK (\(gradleTask))")
    let buildStatus = run(gradlew, [gradleTask], cwd: projectDir, env: ["ANDROID_HOME": androidHome, "ANDROID_SDK_ROOT": androidHome])
    guard buildStatus == 0 else { fail("./gradlew \(gradleTask) failed (status \(buildStatus))") }

    let variantDir = buildRelease ? "release" : "debug"
    let outputsDir = "\(projectDir)/app/build/outputs/apk/\(variantDir)"

    // Check for APK in the variant directory, including possible subdirectories (flavors)
    func findAPK(in dir: String) -> String? {
        let contents = (try? fileManager.contentsOfDirectory(atPath: dir)) ?? []
        // Look for .apk files in the current dir
        if let apk = contents.first(where: { $0.hasSuffix(".apk") && !$0.contains("-unsigned") }) {
            return "\(dir)/\(apk)"
        }
        // Look in subdirectories (flavors)
        for item in contents {
            var isDir: ObjCBool = false
            let path = "\(dir)/\(item)"
            if fileManager.fileExists(atPath: path, isDirectory: &isDir), isDir.boolValue {
                if let apk = findAPK(in: path) {
                    return apk
                }
            }
        }
        return nil
    }

    guard let foundAPK = findAPK(in: outputsDir) else {
        fail("No .apk found in \(outputsDir) after build")
    }
    apkPath = foundAPK
}

// Copy to a friendly, version-labeled name (e.g. "Ventus 1.0.1 (42).apk") at the
// project root so it's easy to find and attach to an email.
if let versionName = readVersionName(), let versionCode = readVersionCode() {
    let friendlyPath = "\(projectDir)/Ventus \(versionName) (\(versionCode)).apk"
    try? fileManager.removeItem(atPath: friendlyPath)
    do {
        try fileManager.copyItem(atPath: apkPath, toPath: friendlyPath)
        apkPath = friendlyPath
    } catch {
        print("Note: could not create friendly-named copy at \(friendlyPath): \(error)")
    }
}

print("APK: \(apkPath)")

guard shouldInstall else {
    print("\n✅ Built \(apkPath). Skipping install (--install).")
    exit(0)
}

guard fileManager.fileExists(atPath: adbPath) else {
    fail("adb not found at \(adbPath). Set ANDROID_HOME/ANDROID_SDK_ROOT or pass --install.")
}

// 3. Check for a connected device
step("Checking for a connected Android device")
let devicesOutput = capture(adbPath, ["devices"])
let deviceLines = devicesOutput
    .split(separator: "\n")
    .dropFirst() // "List of devices attached"
    .map { $0.trimmingCharacters(in: .whitespaces) }
    .filter { !$0.isEmpty }
let connectedDevices = deviceLines.filter { $0.hasSuffix("device") }
guard !connectedDevices.isEmpty else {
    fail("""
    No authorized Android device found. Connect your phone via USB (or Wi-Fi debugging), \
    enable USB debugging in Developer options, and accept the "Allow USB debugging" prompt.

    Current `adb devices` output:
    \(devicesOutput)
    """)
}
if connectedDevices.count > 1 {
    print("Multiple devices detected, installing on all of them:")
}
for device in connectedDevices {
    print(" - \(device)")
}

// 4. Install
for device in connectedDevices {
    let serial = device.split(separator: "\t").first.map(String.init) ?? device
    step("Installing on \(serial)")
    let installStatus = run(adbPath, ["-s", serial, "install", "-r", apkPath])
    guard installStatus == 0 else { fail("adb install failed on \(serial) (status \(installStatus))") }
}

print("\n✅ Installed \(apkPath) on \(connectedDevices.count) device(s).")
