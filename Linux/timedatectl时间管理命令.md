# centOS上的时间管理命令timedatectl

## 1 timedatectl命令可选项

``` 
$ timedatectl --help
timedatectl [OPTIONS...] COMMAND ...

Query or change system time and date settings.

  -h --help                Show this help message
     --version             Show package version
     --no-pager            Do not pipe output into a pager
     --no-ask-password     Do not prompt for password
  -H --host=[USER@]HOST    Operate on remote host
  -M --machine=CONTAINER   Operate on local container
     --adjust-system-clock Adjust system clock when changing local RTC mode

Commands:
  status                   Show current time settings
  set-time TIME            Set system time
  set-timezone ZONE        Set system time zone
  list-timezones           Show known time zones
  set-local-rtc BOOL       Control whether RTC is in local time
  set-ntp BOOL             Control whether NTP is enabled
```

## 2 使用案例

### 2.1 查看当前系统时间、时区
命令 `timedatectl` 或 `timedatectl status` 

``` 
$ timedatectl 
      Local time: Thu 2018-10-11 13:03:04 CST
  Universal time: Thu 2018-10-11 05:03:04 UTC
        RTC time: Thu 2018-10-11 01:17:11
       Time zone: Asia/Shanghai (CST, +0800)
     NTP enabled: no
NTP synchronized: no
 RTC in local TZ: no
      DST active: n/a

$ timedatectl status
      Local time: Thu 2018-10-11 13:03:09 CST
  Universal time: Thu 2018-10-11 05:03:09 UTC
        RTC time: Thu 2018-10-11 01:17:16
       Time zone: Asia/Shanghai (CST, +0800)
     NTP enabled: no
NTP synchronized: no
 RTC in local TZ: no
      DST active: n/a
```

### 2.2 列出全世界所有的时区

命令： `timedatectl list-timezones | grep Asia` 

``` 
# timedatectl list-timezones | grep Asia
...
Asia/Hong_Kong
Asia/Shanghai
Asia/Taipei
Asia/Urumqi
...

```

## 3 设置时区

``` 
# 方法1：
# 将时区设置为上海
$ timedatectl set-timezone Asia/Shanghai

# 方法2：
# 直接修改符号链接
$ rm /etc/localtime
$ ln -s ../usr/share/zoneinfo/Asia/Shanghai /etc/localtime
```

## 4 设置时间

``` 
# 方法1：使用timedatectl，NTP enabled: yes时，使用了NTP服务器自动同步时间，若坚持要手动修改时间，先timedatectl set-ntp no。
# 设置日期和时间
$ timedatectl set-time '2018-10-11 09:00:00'
# 设置日期
$ timedatectl set-time '2018-10-11'
# 设置时间
$ timedatectl set-time '09:00:00'

# 方法2：使用date
$ date -s '2018-10-11 09:00:00'
```

## 5 同步系统时间到硬件时间

``` 
# 方法1：不建议硬件时间随系统时间变化
# 设置硬件时间随系统时间变化
$ timedatectl set-local-rtc 1
# 设置硬件时间不随系统时间变化
$ timedatectl set-local-rtc 0

# 方法2：
$ hwclock --systohc
```

## 6 是否启用自动同步时间

``` 
# 启用|停用自动同步时间
$ timedatectl set-ntp yes|no

# 上面的命令其实是启用、停用时间服务器，若安装了chrony服务，则等同于对该服务启停，若只安装了ntp，则是对ntp服务启停。
# 对chrony服务启停
$ systemctl start|stop chronyd
# 对ntp服务启停
$ systemctl start|stop ntpd
```

## 5. 自动同步时间：服务ntp、chrony，命令ntpdate

CentOS7之前采用ntp服务自动同步时间，CentOS7推荐使用chrony同步时间，当然ntp仍然可以使用，chrony官网列举了诸多chrony优于ntp的功能（ntp与chrony的对比：https://chrony.tuxfamily.org/comparison.html）。
此外我们若要立刻将系统时间同步为NTP服务时间，使用ntpdate命令，也可以配置计划任务定期使用ntpdate命令同步时间，从而就不用使用ntp或chrony服务，减少监听的端口，增加系统安全性。

### 5.1 ntp的安装配置

安装ntp
`$ yum -y install ntp` 

配置ntp同步阿里云时间服务器

``` 
$ vim /etc/ntp.conf
...
server ntp1.aliyun.com
server ntp2.aliyun.com
server ntp3.aliyun.com
#server 0.centos.pool.ntp.org iburst
...
```

启动ntp服务器

``` 
$ systemctl start ntpd
```

### 5.2 chrony的安装配置

安装chrony
`$ yum -y install chrony
`

配置chrony

``` 
$ vim /etc/chrony.conf
server ntp1.alyun.com
server ntp2.alyun.com
server ntp3.alyun.com
#server 0.centos.pool.ntp.org iburst
...
```

启动chrony
`$ systemctl start chronyd` 

### 5.3 配置计划任务，使用ntpdate同步时间

``` 
# 启动并开机启动计划任务cron
$ systemctl start crond
$ systemctl enable crond

# 配置计划任务，每5分钟同步一次
$ crontab -e
*/5 * * * * /usr/sbin/ntpdate ntp1.aliyun.com
```

原文连接：https://www.cnblogs.com/zhubiao/p/9768209.html

