# Reload Everything

Fabric 客户端模组：给三个界面各加一个 ⟳ 按钮，一键重载。

| 位置 | 动作 |
| --- | --- |
| 主菜单 | 重启游戏进程：用当前进程的真实 argv（含 -Xmx 等 JVM 参数）拉起新实例，退出旧实例 |
| 暂停菜单（仅单机） | 保存并重新加载当前存档；装不装 FastQuit 都会等存档落盘完成 |
| 资源包界面 | 重载资源包，并回到打开资源包界面之前的那个界面 |

三个动作都会先弹出二次确认。

## 版本

- Minecraft 26.3
- Fabric Loader 0.19.3
- Fabric API 0.161.0+26.3
- Java 25

## 构建

Windows: gradlew.bat build    其他平台: ./gradlew build

产物在 build/libs/ 下。

## 可选依赖

FastQuit：安装后重载存档会用它阻塞等待保存完成；没装则轮询等待（最长 15 秒），超时取消重载以避免损坏存档。

## 开发环境说明

进程级重启在开发环境（Fabric 开发运行）下会被跳过，只做软重载。

## 许可

MIT
