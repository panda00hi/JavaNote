原文地址：
https://blog.csdn.net/pinnuli/article/details/81293071

我们在使用git管理代码的时候，经常需要放到不同的托管网站，如github，osc等，那么不同的网站账号不一样，就需要生成不同密钥，配置对应的不同网站，接下来我们写写如何处理。

    ps:这里是在centos7.2下操作，不过其他操作系统依然适用，这里举的例子，一个是github，一个是osc。

1 生成密钥

这里可以设置密钥文件名和路径，/root/.ssh 是路径（一般路径选择默认），id_rsa_github是密钥文件名, 文件命名后按两次回车，即密码为空

`ssh-keygen -T rsa -C "example@qq.com" ` 

生成github的密钥

![img](https://img-blog.csdn.net/20180730215922967?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

生成osc的密钥

![img](https://img-blog.csdn.net/20180730215956766?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

查看一下.ssh文件夹，发现有
id_rsa_github, id_rsa_github.pub（放到github）, id_rsa_osc, id_rsa_osc.pub（放到osc)

`ls -a /root/.ssh ` 

2 接下来配置多账号

在.ssh文件夹下面新建一个命名为config的文件，编辑如下内容

``` 
#github
       Host github.com    
       HostName github.com
       IdentityFile ~/.ssh/id_rsa_github
       User pinnuli

#osc
       Host gitee.com
       HostName gitee.com
       IdentityFile ~/.ssh/id_rsa_osc
       User pinnuli
```

3 把对应的公钥放到github和osc上面

![img](https://img-blog.csdn.net/20180730220138843?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

![img](https://img-blog.csdn.net/20180730220154715?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

4 测试是否成功

ssh -T git@github.com

![img](https://img-blog.csdn.net/20180730220222735?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

`ssh -T git@gitee.com` 

![img](https://img-blog.csdn.net/20180730220247435?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3Bpbm51bGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

