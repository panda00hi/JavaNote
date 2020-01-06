## linux下的目录

Linux 的软件安装目录是也是有讲究的，理解这一点，在对系统管理是有益的

/usr：系统级的目录，可以理解为C:/Windows/，/usr/lib理解为C:/Windows/System32。

/usr/local：用户级的程序目录，可以理解为C:/Progrem Files/。用户自己编译的软件默认会安装到这个目录下。

/opt：用户级的程序目录，可以理解为D:/Software，opt有可选的意思，这里可以用于放置第三方大型软件（或游戏），当你不需要时，直接rm -rf掉即可。在硬盘容量不够时，也可将/opt单独挂载到其他磁盘上使用。

源码放哪里？

/usr/src：系统级的源码目录。

/usr/local/src：用户级的源码目录。

-----------------翻译-------------------

/opt

Here’s where optional stuff is put. Trying out the latest Firefox beta? Install it to /opt where you can delete it without affecting other settings. Programs in here usually live inside a single folder whick contains all of their data, libraries, etc.

这里主要存放那些可选的程序。你想尝试最新的firefox测试版吗? 那就装到/opt目录下吧，这样，当你尝试完，想删掉firefox的时候，你就可 以直接删除它，而不影响系统其他任何设置。安装到/opt目录下的程序，它所有的数据、库文件等等都是放在同个目录下面。

举个例子：刚才装的测试版firefox，就可以装到/opt/firefox_beta目录下，/opt/firefox_beta目录下面就包含了运 行firefox所需要的所有文件、库、数据等等。要删除firefox的时候，你只需删除/opt/firefox_beta目录即可，非常简单。

/usr/local

This is where most manually installed(ie.outside of your package manager) software goes. It has the same structure as /usr. It is a good idea to leave /usr to your package manager and put any custom scripts and things into /usr/local, since nothing important normally lives in /usr/local.

这里主要存放那些手动安装的软件，即不是通过“新立得”或apt-get安装的软件。它和/usr目录具有相类似的目录结构。让软件包管理器来管理/usr目录，而把自定义的脚本(scripts)放到/usr/local目录下面，我想这应该是个不错的主意。

## linux下手动安装软件，并创建桌面、菜单中的图标（以火狐[国际版]为例）

### 1、安装

https://www.mozilla.org/en-US/firefox/all/#product-desktop-release

注意要选择Mozilla系列，否则为中文版，国内公司代理的，有各种广告

下载完成后，到本地解压 终端运行命令

    tar -xjvf firefox-70.0.1.tar.bz2 

得到完整的程序包，此时其实已经可以运行了，运行firefox目录下的firefox可执行文件即可运行

但是为了更加符合使用习惯，也方便日常管理（Linux一切皆文件，如果不注意，会导致乱七八糟），我们把程序包放到opt目录下（opt目录可以类比Windows下的c:\programfiles）可以放置我们安装的一些软件。

终端运行命令

    mv firefox /opt

在opt目录下我们已经看到firefox的程序包。

接下来，开始优化firefox的桌面、菜单的图标

### 2、配置图标

终端运行命令：

    sudo vim /usr/share/applications/firefox.desktop

此时会新建一个vim文档，写入如下内容

    [Desktop Entry]
    Name=Firefox
    Exec=/opt/firefox/firefox
    # 注意保证这个图标实际存在。可以查看程序包实际的图标位置，不同版本可能位置不一样
    Icon=/opt/firefox/browser/chrome/icons/default/default64.png
    Terminal=false
    Type=Application
    Categories=Application;Network;

保存退出，在程序列表已经可以发现firefox的图标了

此外也可以再进行其他的一些快捷方式的设置等。

扩展

实际上相当于对图标进行了一个链接

    # 每个desktop文件都已这个标签开始，说明这是一个Desktop Entry 文件.
    [Desktop Entry]
    # 标明Desktop Entry的版本(可选).
    Version=1.0
    # desktop的类型(必选),常见值有“Application”和“Link”.
    Type=Application
    # 程序名称(必须)                 
    Name=MindMaster 
    # 程序描述(可选).                 
    GenericName=Mind Master
    # 程序描述(可选).     
    Comment=Mind mapping
    # 程序的启动命令(必选),可以带参数运行    
    Exec=/home/l/APP/mindmaster-6-amd64-cn/MindMaster-6-x86_64
    # 设置快捷方式的图标(可选).            
    Icon=/home/l/APP/mindmaster-6-amd64-cn/mindmaster.png
    # 是否在终端中运行(可选),当Type为Application,此项有效. 
    Terminal=false
    # 注明在菜单栏中显示的类别(可选)
    Categories=Application;Network

### linux下apt-get install安装的软件目录

使用apt-get clean后，下载的软件包会删除	

下载的软件存放位置   /var/cache/apt/archives   

安装后软件默认位置   /usr/share   

可执行文件位置    /usr/bin   

配置文件位置   /etc   

lib文件位置   /usr/lib

