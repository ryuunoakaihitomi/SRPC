# 存储运行时权限控制器 (Storage Runtime Permission Control)

## 背景

* 为什么需要这个应用

部分Android 6.0国产系统对运行时权限做了改动，有些甚至完全取消了存储权限的运行时权限拦截机制（表现如同Android 5.1之前完全无法控制存储权限）。笔者不幸获得一部这样的设备，不得不使用第三方的存储权限管理应用。

* 为什么不使用同类已经久负盛名的权限管理应用（如App Ops和权限狗）

它们最低仅支持Android 7.0，App Ops似乎找到最后支持Android 6.0的历史版本带有谷歌广告。

## 原理

[`DevicePolicyManager#setPermissionGrantState`](https://developer.android.google.cn/reference/android/app/admin/DevicePolicyManager#setPermissionGrantState(android.content.ComponentName,%20java.lang.String,%20java.lang.String,%20int))

* 为什么强制Device Owner和不使用[testdpc](https://github.com/googlesamples/android-testdpc)

**怕手贱**

* 有什么限制 （⚠注意事项）

文档和AVD实测结果表明，通过本应用设置后就不能通过原有的权限设置UI更改存储权限，不过我这本来就没有这个选项所以没有关系。

> The state can be default in which a user can manage it through the UI, denied, in which the permission is denied and the user cannot manage it through the UI, and granted in which the permission is granted and the user cannot manage it through the UI.

本应用卸载后效果仍然保留。

默认(default)权限是给用户自由控制存储权限（可以通过设置UI更改），但权限状态本身可能不会更改。

> Setting the grant state to default does not revoke the permission. It retains the previous grant, if any.