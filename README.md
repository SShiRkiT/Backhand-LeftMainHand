# Backhand - Left Main Hand Fork

> ⚠️ **本 Fork 修改内容 / What this fork does:**
>
> 在原版 Backhand 1.8.12 基础上添加了**左手主手模式**（类似现代 Minecraft 高版本的左手选项）。
> 通过配置文件 `backhand/client.cfg` 中的 `LeftHandedMode = true` 开启后，
> 主手物品将渲染在屏幕左侧，副手物品渲染在屏幕右侧。
>
> Adds a **left-handed main hand mode** (similar to modern Minecraft's left-hand option), based on original Backhand 1.8.12.
> Enable via `LeftHandedMode = true` in `config/backhand/client.cfg`.
> The main hand renders on the left side, offhand on the right.
>
> ---
>
> ## ⚠️ 版本说明 / Version Note
>
> Release 中提供 **两个版本**：
>
> | 版本号 | 实际代码 | 用途 |
> |--------|----------|------|
> | `1.8.12` | 基于原版 Backhand 1.8.12 修改 | **真实版本号**，用于开发 / PR / 新版 GTNH |
> | `1.7.7` | 与 1.8.12 完全相同，仅版本号改为 1.7.7 | **伪装版本号**，用于 GTNH 2.8.4 旧整合包进服务器 |
>
> GTNH 2.8.4 的服务器要求 `backhand@[1.7.2,)`，不认 1.8.12。所以打包了一个版本号写死为 `1.7.7` 的 jar 来绕过检测。**两者代码完全一致。**
>
> Two versions are provided in the Release:
> - `1.8.12` — Real version. For development / PR / newer GTNH versions.
> - `1.7.7` — Fake version number (same code as 1.8.12). For GTNH 2.8.4 servers that reject 1.8.12.
>
> ---
>
> 🤖 **完全由 AI 进行修改，仅保证 Backhand 在 GTNH 2.8.4 中的运行效果。不保证其他版本的可用性**
>
> 🤖 **All modifications made entirely by AI. Only guaranteed to work with Backhand on GTNH 2.8.4. Not guaranteed of availability for other versions**

---

# Backhand

A minimalist fork of [The Offhand Mod](https://github.com/TCLProject/theoffhandmod), which itself is forked from [Mine&Blade Battlegear 2](https://github.com/Mine-and-blade-admin/Battlegear2), that backports a sole offhand slot with functionality close to vanilla Minecraft's later versions' offhand. No extra items are added from Battlegear2, but extra functionality has been added for the offhand such as a blacklist and allowing attacking with the offhand. These functions are disabled by default and can be toggled through the config file.

Big thanks to TCLProject, the creator of the offhand mod. Another huge thanks to nerd-boy & GotoLink as well, the authors of Mine&Blade Battlegear 2.

## Incompatibilities
* Mine&Blade Battlegear 2
