## Java

* Download: http://www.oracle.com/technetwork/java/javase/downloads/index.html
* Version: [Java SE 8u121](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* Install
* 环境变量: 

> Java 安装本身是一步流程安装，并不需要配置环境变量即可使用，但是考虑到某些软件需要环境的支持，所以需要配置一下。

1. 首先打开用户目录下的 **.bash_profile** 没有则使用 touch 创建。

2. 添加相关配置: 

   ```
   JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_121.jdk/Contents/Home
   CLASSPAHT=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
   export PATH=$JAVA_HOME/bin:$PATH
   export JAVA_HOME
   export CLASSPATH
   ```

3. 保存并更新配置: 

   ```
   source .bash_profile
   ```



## Tomcat

* Web: http://tomcat.apache.org/

* Version: [ 9.0.0.M17](http://tomcat.apache.org/download-90.cgi)

* 解压: **~/Library/Tomcat**

* 环境变量添加

* ```
  export PATH=/Users/qiujuer/Library/Tomcat/bin:$PATH
  ```

* 执行命令:  **startup.sh** 尝试启动

* 访问:  **http://localhost:8080/**



## Gradle

* Web: https://gradle.org/install
* Version: gradle 3.3
* Install:  `$ brew install gradle` 
* Download: https://services.gradle.org/distributions/gradle-3.3-bin.zip
* 环境变量：


* ```
  GRADLE_HOME=/Users/qiujuer/.gradle/wrapper/dists/gradle-3.3-all/55gk2rcmfc6p2dg9u9ohc3hw9/gradle-3.3;
  export GRADLE_HOME
  export PATH=$PATH:$GRADLE_HOME/bin
  ```