# car_pooling

# 技术栈：

需要额外学习的东西：SmartDoc（一款api文档生成器）

# 规范

## 基础规范：

​	在哪里启动项目？

​	在start模块的AppRun这个类中启动整个SpringBoot项目



​	最后的启动项是car_pooling_start。各种启动参数应写在这个模块的resources下。



​	根据环境的不同选择不同的启动环境，如何选择？在properites文件中选择pro(生产环境)/dev(开发环境)



​	本项目兼容了mybatis配置，但是尽量不要使用xml，过于冗余，即使要连表查询最好是直接用注解实现。（是在不行要写XXMapper.xml的话应该是在对应项目的resoucres下创建一个文件夹mapper。在这个文件夹下面编写对应的xml文件。



## 模块的命名：

​	打开目录，项目的命名规则是car_pooling_XXX，其中xxx是这个模块的责任，如common模块就是本项目的基础功能包。



## 每个模块的中结构的命名和规范：

​	在main中的有俩个文件夹，一个是java，另一个是resource，除非是在start模块，不然不要在resource下面写任何的配置文件。第二个是java文件夹，文件夹的格式应该是com.carpooling.XXX。XXX是你的模块名，这个就是主结构，剩下的就是在XXX下面去创建对应的文件夹，结构可以参照common模块。

​	自行了解Springboot的基础项目结构。service、serviceimpl、Mapper。





##  Common模块的规范：

​	因为这个模块是我直接从我上一个项目拉下来的因此有几点需要解释清楚。

​	第一点：使用的是Jackson进行序列化，也就是JSON对象和java对象进行互换。

​	第二点：后端的ID一般都是Long类型，但是Long类型前端接受的时候会出现精度丢失，这个已经解决好了。

​	第三点：就是全局异常处理器，自行了解。

​	第四点：Redis的前缀静态类，要进行使用。

### pojo文件夹：

​	这个文件夹存放着基础的ORM对象，也就是每张数据库表的对象的基础的三个属性，任何的ORM对象都应该继承这个对象。

​	如果有些对象需要多个模块进行引用，可以将其从本身的模块提到这个模块中，但是要看**注意事项**

​	通用返回对象R。 

### exception文件夹：

​	存放的是自定义的异常和全局异常处理器。

### config文件夹：不允许动



## 命名规范：

### Redis业务的命名规范

​	Key的命名规范：

​		业务:标识........

​		例如： 查询业务，有个人的id是123

​		search:123

### 和对象的命名规范：

​	和前端进行交互的对象命名应该是以VO为结尾。

​			例如StudentVO，TeacherVO。

​	和Redis交互的对象**可以使用DTO、VO**进行结尾

​	和数据库交互的应该是和表名一致。

# 项目结构

（持续更新）

- car_pooling
  - car_pooling_common
  - car_pooling_start
  - car_pooling_monitor
  - car_pooling_order
  - car_pooling_phonecertification
  - car_pooling_stucertification

# 模块的职责和注意事项

## common

提供基础功能和配置文件

## monitor

使用canal对数据库指定的表进行监听。

## order

订单模块

## phonecertification

电话认证模块

## stucertification

学生认证模块

## start

启动模块。实现一些其他认证。

# 注意事项

**任何对于`Common模块`和`配置文件`的更改都要互相通知，这些东西是本项目的基础模块，一个改变会导致全局改变。**

