package com.lkl.cloudpos.aidl.cpucard;

interface AidlCPUCard{
 /*
   cardType: 卡片类型，在com.lkl.cloudpos.data.CPUCard.CPUCardType类中拉卡拉已定义了卡片类型
   返回值：
         0 打开卡成功
         1 对于cardType暂不支持
         2 打开卡失败
   打开设备，open方法必须对cardType进行检验，如果不支持cardType类型，返回1
 */
 int open(int cardType);
 /*关闭设备*/
 void close();
 /*修改卡片总密码， oldpsw和newpsw具体格式必须遵循具体卡片格式*/
 boolean changePassword(in byte[] oldpsw, in byte[] newpsw);
 /*读卡： psw 卡片总密码：当卡片不需要读保护时，可以传null
 offset：开始地址  length 读取长度
 成功返回读取的数据，失败返回null
 */
 byte[] read(in byte[] psw, int offset, int length);
 /*写卡： psw 卡片总密码：当卡片不需要写保护时，可以传null
   offset：开始地址  data 要写入的数据
  成功返回true，失败返回false
  */
 boolean write(in byte[] psw, int offset, in byte[] data);

 /*
    cardType: 卡片类型，在com.lkl.cloudpos.data.CPUCard.CPUCardType类中拉卡拉已定义了卡片类型
    返回值：
          0 打开卡成功
          1 对于cardType暂不支持
          2 打开卡失败
    打开设备，根据上电后获取的卡片ATR值与传入的ATR值进行比较，如果不相等返回1
  */
  int openWithATRVerification(int cardType, in byte[] atrData);
}