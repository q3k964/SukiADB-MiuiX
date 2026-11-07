# SukiADB → MiuiX / KernelSU 界面改造说明

本目录是 **SukiADB**（AxManager 换皮版）的源码，界面已从「液态玻璃 + 悬浮胶囊底栏」改造成
**KernelSU / MiuiX 风格**：大标题、纯白扁平卡片、灰色页面底、平底栏。

---

## 一、改了哪些文件

| 文件 | 改动 |
|---|---|
| `ui/theme/Color.kt` | 配色整体重写：强调色默认改为系统蓝 `#2196F3`；浅色 = 灰底 `#F5F5F5` + 纯白卡片；深色 = `#121212` + `#202020` 卡片。删掉原来基于 HSL 推演的「Vortex」渐变配色，改为扁平语义色 |
| `ui/theme/Type.kt` | 新增完整 MiuiX 字号体系。关键项 `headlineLarge` = 34sp/Medium（KSU 的巨型页面标题），`titleMedium` = 17sp/Medium（列表主标题） |
| `ui/theme/Theme.kt` | 圆角改为 8/10/14/20/28dp；主题函数签名保持 `AxManagerTheme(contentCompose)` 不变 |
| `ui/component/AxCard.kt` | **卡片改为纯扁平**：`surfaceContainerHigh` 纯白、无 border、无阴影、无渐变、无玻璃。`IconBadge` 去掉彩色底块，改成纯线条图标 |
| `ui/component/AxTopAppBar.kt` | 顶栏拆成 MiuiX 的两段式：48dp 图标行（返回 + 操作）+ 下方巨型标题。无图标时标题独占 |
| `ui/component/ThemePreview.kt` | **新增**：KSU 主题页那个手机模型预览卡 + 三段式 跟随系统/浅色/深色 选择器 |
| `ui/component/SettingsItem.kt` | 设置行：28dp 线条图标、14dp 间距、16sp 常规字重、细分隔线 |
| `ui/component/SearchBar.kt` | 搜索框从胶囊改成圆角矩形，底色用更深的灰 |
| `ui/AxActivity.kt` | **底栏重写**：删掉悬浮胶囊 + 液态玻璃（`kyant.backdrop`），改成 KSU 的四等分平底栏（图标在上、文字在下、仅用颜色表示选中）；FAB 移到右下角浮在底栏之上；删除 10 个 kyant import |
| `ui/screen/Home.kt` | **首页重构成 KSU 双列网格**：左侧大色块状态瓦片（浅蓝绿=运行中 / 浅红=未运行）+ 右侧两个白色计数瓦片（超级用户 / 模块）。删除 12 个不再使用的私有 Composable |
| `ui/screen/Appearance.kt` | 外观页顶部接入主题预览卡 + 三段选择器，替换原来的「自动主题」开关 + 单选列表 |
| `ui/screen/{Activate,Appearance,Developer,PrivilegeDetail,QuickShell,Settings,plugin/Plugin}.kt` | 顶栏标题统一升为 `headlineLarge` |
| `ui/screen/Privilege.kt` | 应用列表行：16dp 边距、48dp 图标、16dp 间距、Medium 字重 |
| `gradle/libs.versions.toml` + `manager/build.gradle.kts` | **删除** `liquid-glass`（`kyant0:backdrop-android`）依赖，已无任何引用 |
| `res/values/strings.xml` + `res/values-zh-rCN/strings.xml` | 新增 3 条：`theme_follow_system` / `theme_light` / `theme_dark` |
| `.github/workflows/build-sukiadb.yml` | **固定 API 子模块**到上游 commit `37eb6a3`（原来每次拉 master 最新，构建不可复现） |

**未改动**：`server/`、`adb/`、`reignite/`、`plugins/`、`api/`（全部业务逻辑与 native 代码原样保留）。
这次改造只碰 UI 层，功能行为与 SukiADB 完全一致。

---

## 二、构建产物

**已构建成功**，仓库地址：<https://github.com/q3k964/SukiADB-MiuiX>

CI 第 1 次构建失败（2 处编译错误，已修复）：
- `AxCard.kt` 的 `AxScrollbarBubbleShape` 用了裸 `Int`（`RoundedCornerShape` 需要 `Dp`）
- `Home.kt` 残留了一个孤立 `@Composable` 注解

第 2 次构建通过，产出 4 个 ABI 的 APK：

| 文件 | 大小 | 适用 |
|---|---|---|
| `...-universal_*.apk` | 6.7 MB | 通用（不确定机型时用这个） |
| `...-arm64-v8a_*.apk` | 4.6 MB | 绝大多数现代手机 |
| `...-armeabi-v7a_*.apk` | 4.4 MB | 老 32 位设备 |
| `...-x86_64_*.apk` | 4.6 MB | 模拟器 |

包名 `frb.axeron.sukiadb`，版本 `v3.0.0.r1 (30000)`。

### 重新构建

本机（Android proot 环境）**构建不了**：无 JDK、无 Android SDK、无 NDK，可用内存约 1.3 GB。
走 GitHub Actions：

```bash
cd src
git init
git add -A
git commit -m "MiuiX UI port"
git remote add origin https://github.com/<你的用户名>/<仓库名>.git
git push -u origin main
```

推到之后进仓库 **Actions** 页，等 `Build SukiADB MiuiX APK` 跑完，
在运行记录的 **Artifacts** 下载 `SukiADB-MiuiX-APK`（保留 30 天）。

workflow 已处理三件事：
- 手动 clone `Axeron-API` 子模块并 **checkout 固定 commit**（zip 里不带 submodule）
- `fetch-depth: 0`（`versionName` 依赖 `git rev-list --count HEAD`）
- 自动生成签名密钥并写 `local.properties`

### 本地构建（需要 8 GB+ 内存的电脑）

需要 **JDK 21**、**Android SDK 37**、**NDK 29.0.14206865**、**CMake 3.22.1**。

```bash
cd src
# 1. API 子模块（若 zip 里没有）
rm -rf api && git clone https://github.com/fahrez182/Axeron-API.git api
git -C api checkout 37eb6a3521828458394797e7166ff2e90946304e

# 2. 签名
keytool -genkeypair -keystore release.keystore -alias sukiadb \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass sukiadb -keypass sukiadb -dname "CN=SukiADB"

# 3. local.properties
cat > local.properties <<EOF
sdk.dir=$HOME/Android/Sdk
api_version_code=30000
api_version_name=3.0.0
signing.storeFile=$PWD/release.keystore
signing.storePassword=sukiadb
signing.keyAlias=sukiadb
signing.keyPassword=sukiadb
EOF

# 4. 编译
chmod +x gradlew && ./gradlew :manager:assembleRelease
```

产物：`manager/build/outputs/apk/release/`

---


## 液态浮动标签栏（AndroidLiquidGlass）

按 <https://github.com/Kyant0/AndroidLiquidGlass> 接入，效果对齐库 demo app 里的
`LiquidBottomTabs`：**悬浮胶囊玻璃条 + 选中项液态透镜放大 + 可拖动切换**。

### 依赖（全部官方 Maven，库源码不改）

```toml
liquid-glass      = "io.github.kyant0:backdrop-android:2.0.1"
kyant-shapes      = "io.github.kyant0:shapes-android:1.2.1"      # Capsule 形状
coroutines-android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2"
```

### 新增文件 `ui/component/liquid/`

| 文件 | 来源 |
|---|---|
| `LiquidBottomTabs.kt` | 照搬 catalog 的 `LiquidBottomTabs`，仅把 accent 色改为跟随 App 主题 |
| `LiquidBottomTab.kt` | 照搬 catalog，`2f.dp` 改为 `2.dp` |
| `DampedDragAnimation.kt` | 照搬 catalog utils（拖动阻尼动画） |
| `InteractiveHighlight.kt` | 照搬 catalog utils（按压高光） |
| `DragGestureInspector.kt` | 照搬 catalog utils（拖动手势检测） |

注意：这几个组件在库的 **demo app** 里，不在发布的 `backdrop` 库中，所以要复制源码。

### 三层结构

1. **玻璃胶囊**：`vibrancy() + blur(8dp) + lens(24dp)`，`Capsule()` 形状
2. **隐形副本**：同样内容画成透明层，用 `ColorFilter.tint(强调色)` 染色，供下层采样
3. **透镜药丸**：`lens(10dp, 14dp, chromaticAberration = true)`，跟随选中项，放大底下内容

按压时整体 `scaleY` 微缩放，透镜药丸还有 `Shadow` + `InnerShadow`。

### 拖动切换

拖动胶囊可左右滑动，透镜跟着走；松手吸附到最近标签并切换页面。
`DampedDragAnimation` 用弹簧动画，`InteractiveHighlight` 提供按压时的径向高光。

### 内容区左右滑动

内容区左右滑也能切页，判定逻辑：24dp 阈值判断主方向，横向位移需超过纵向 1.5 倍才接管，
纵向优先时让给列表滚动。滑动超过 80dp 才切页。

### 已修复的 3 个崩溃/编译问题

1. `UnsupportedOperationException: Only RoundedRectangularShape or CornerBasedShape...`
   → `lens()` 反推圆角半径时 `RectangleShape` 返回 null，改用 `RoundedCornerShape` / `Capsule`
2. `Platform declaration clash: setGlassOpacity(F)V`
   → `var glassOpacity` 的自动 setter 与显式方法撞签名，属性改名 `glassBarOpacity`
3. `Unresolved reference 'awaitFrame'`
   → 缺 `import kotlinx.coroutines.android.awaitFrame`，且项目未声明 `coroutines-android`

## 三、视觉对照（截图里那套）

| 元素 | KernelSU（截图） | 本次改造后 |
|---|---|---|
| 页面标题 | 「设置」「模块」超大黑字 | `headlineLarge` 34sp Medium，左对齐 |
| 卡片 | 纯白、大圆角、无边框无阴影 | `surfaceContainerHigh` 纯白 + 20dp 圆角，无 border/elevation |
| 页面底色 | 浅灰 | `#F5F5F5` |
| 首页状态 | 左绿色块「工作中 \<LKM\>」+ 右两个计数卡 | 左 `primaryContainer` 色块 + 右两个白色 `CounterTile` |
| 底栏 | 平底、四等分、图标+文字、无胶囊 | 四等分 `Row`，64dp 高，选中仅变色 |
| 设置行 | 线条图标 + 标题 + 描述 + 开关 | 28dp 线条图标，16sp 常规，`Switch` |
| 搜索框 | 灰色圆角矩形 | `AxPanelShape` + `surfaceContainerHighest` |
| 主题页 | 手机模型预览 + 三段选择器 | `ThemePreviewCard` + `ThemeModeSelector` |

深色模式同样适配：页面 `#121212`、卡片 `#202020`、文字 `#EDEDED`。

---

## 四、注意事项

1. **没有实机验证过**。本机无法编译，改完只做了静态校验（括号平衡、import 完整性、
   颜色角色覆盖、API 签名比对）。首次 CI 构建若有编译错误，看 Actions 日志的
   `:manager:compileDebugKotlin` 段即可定位。
2. **保留 SukiADB 原有问题**：`KeepAliveService` 仍默认开启并自我加入 Doze 白名单；
   签名仍是 CI 每次生成的一次性密钥；与原版 AxManager 共存的运行时冲突依旧存在
   （共用 `/data/adb/axeron` 和 `axeron_server` 进程）。这些是上一轮分析里指出的问题，
   本次只改 UI，未触碰。
3. **`ThemePreviewCard` 是纯 Compose 绘制**，不依赖图片资源，会跟随当前强调色和明暗模式变化。
4. 想调回原来的圆角/配色，只改 `theme/Theme.kt` 的 `AxShapes` 和 `theme/Color.kt` 里
   的 `lightPage` / `lightCard` / `darkPage` / `darkCard` 几个常量即可，其余组件都从语义色取。
