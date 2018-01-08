##### 一、XSS：
 1、cross site script：跨站脚本攻击
2、预防XSS:
-  内置转义函数
- DOM解析白名单
- 第三方库
- CSP：Content Security Policy
##### 二、CSRF：
 1、Cross-site request forgery：跨站请求伪造
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