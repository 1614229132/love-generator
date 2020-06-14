配置说明：

1. 修改generator.properties
    *package -> compliance工程包名
    *satpPackage -> clove-develop工程包名
    *basePath -> jsp相对路径
    *satp -> satpService / localService
    *tablePrefix -> 表前缀
     author -> 作者
     email -> 邮箱

2. 修改application.yml
    spring:
        dbType: orcal / mysql

** 注意：建表时请务必添加 “表注释” 及 “字段注释 ” **
