package(default_visibility = ["//visibility:public"])

licenses(["notice"])

load("//intellij_platform_sdk:build_defs.bzl", "select_for_plugin_api")

java_library(
    name = "python_internal",
    visibility = ["//visibility:private"],
    exports = select_for_plugin_api({
        "intellij-2017.3": ["@python_2017_3//:python"],
        "intellij-ue-2017.3": ["@python_2017_3//:python"],
        "intellij-2018.1": ["@python_2018_1//:python"],
        "intellij-ue-2018.1": ["@python_2018_1//:python"],
        "clion-2017.3": ["@clion_2017_3//:python"],
        "clion-2018.1": ["@clion_2018_1//:python"],
        "android-studio-3.0": ["@python_2017_1_4249//:python"],
        "android-studio-3.2": ["@python_2018_1//:python"],
    }),
)

java_library(
    name = "python_for_tests",
    testonly = 1,
    exports = [":python_internal"],
)

java_library(
    name = "python",
    neverlink = 1,
    exports = [":python_internal"],
)
