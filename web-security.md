 1、cross site script：跨站脚本攻击</br>
2、预防XSS:
-  内置转义函数
- DOM解析白名单
- 第三方库
- CSP：Content Security Policy
##### 二、CSRF：
 1、Cross-site request forgery：跨站请求伪造</br>
 2、预防
- Cookie same site属性
- HTTP referer头
- token
##### 三、Cookies-安全策略
 1、安全策略
- 签名防篡改
- 私有变换(加密)
- http-only（防止xss）
- security（https）
- same site
##### 四、点击劫持
- 预防点击劫持：
- - JavaScript禁止内嵌
- - X-FRAME-OPTIONS禁止内嵌
- - 其他辅助手段
##### 五、HTTP传输安全
- HTTP窃听
- - 窃听用于密码
- - 窃听传输敏感信息
- - 非法获取个人资料
- HTTP篡改
- - 插入广告
- - 重定向网站
- - 无法防御的XSS和CSRF攻击