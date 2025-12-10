#!/bin/bash

# 此脚本用于使用 Java 17 运行此项目
# 使用方法：./run-with-java17.sh [maven命令]
# 例如：./run-with-java17.sh spring-boot:run
#      ./run-with-java17.sh clean compile

# 查找 Java 17
JAVA17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)

if [ -z "$JAVA17_HOME" ]; then
    echo "错误: 未找到 Java 17"
    echo ""
    echo "请先安装 Java 17，可以使用以下命令："
    echo "  brew install --cask temurin17"
    echo ""
    echo "或者访问：https://adoptium.net/zh-CN/temurin/releases/?version=17"
    echo ""
    exit 1
fi

echo "使用 Java 17: $JAVA17_HOME"
echo "Java 版本:"
"$JAVA17_HOME/bin/java" -version
echo ""

# 设置 JAVA_HOME 并运行 Maven 命令
export JAVA_HOME="$JAVA17_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# 使用 mvn 命令（Maven 会自动读取 JAVA_HOME 环境变量）
MAVEN_BIN="mvn"

# 如果传入了参数，执行 Maven 命令；否则默认运行 spring-boot:run
if [ $# -eq 0 ]; then
    echo "执行: mvn spring-boot:run"
    "$MAVEN_BIN" spring-boot:run
else
    echo "执行: mvn $@"
    "$MAVEN_BIN" "$@"
fi

