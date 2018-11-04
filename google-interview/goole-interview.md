##### 一、事务：
 ACID
- Automicity
- Consistency
- Isolation
- Durability


隔离级别:
- Read uncommitted
- Read committed
- Repeatable Reads
- Serilizable
##### 二、数值：
- 使用BigDecimal算钱
<code>Objects.equals(a,b)</code>
- 拆箱和装箱
##### 三、编码技巧
- 链表的反转
- 组合问题
- 删除链表中的元素
- 二分查找法处理边界问题（a+(b-a)/2）,顺便查 看java源码如何处理这个问题</br>
（右移一位相当于除以2
<code>int mid = (low + high) >>> 1</code>
）
- 面试树的问题问的比较多，图相对少
- 二叉树的遍历
- 根据前序、遍历中序遍历找出后续遍历</br>
（方法一：建树，方法二：不建树）
![avatar](http://images2015.cnblogs.com/blog/795187/201510/795187-20151023201552927-578458496.png)
```java
    public class Test {
    class TreeNode
    {
        TreeNode left;
        TreeNode right;
        char  value;
    };
    public static void main(String[] args) {
        String preOrder = "GDAFEMHZ";
        String inOrder = "ADEFGHMZ";
        Test test = new Test();
        //不建树
        System.out.println(test.getPostOrder(preOrder,inOrder));;
        //建树
        System.out.println(test.postOrder(test.postOrderGetTree(preOrder,inOrder) ));
    }
    //不建树
    private String getPostOrder(String preOrder, String inOrder){
        if(preOrder==null) return "";
        if(preOrder.length()==0)return inOrder;
        char root = preOrder.charAt(0);
        int inOrderRootIndex = inOrder.indexOf(root);
        return getPostOrder(preOrder.substring(1,inOrderRootIndex+1),inOrder.substring(0,inOrderRootIndex))
                + getPostOrder(preOrder.substring(inOrderRootIndex+1,preOrder.length()),inOrder.substring(inOrderRootIndex+1,preOrder.length()))
                +root;
    }
    //建树
    private TreeNode postOrderGetTree(String preOrder,String inOrder){
        if(preOrder==null || preOrder.length()==0)return null;
        TreeNode root = new TreeNode();
        char rootValue = preOrder.charAt(0);
        root.value = rootValue;
        int inOrderRootIndex = inOrder.indexOf(rootValue);
        root.left = postOrderGetTree(preOrder.substring(1,inOrderRootIndex+1),inOrder.substring(0,inOrderRootIndex));
        root.right = postOrderGetTree(preOrder.substring(inOrderRootIndex+1,preOrder.length()),inOrder.substring(inOrderRootIndex+1,preOrder.length()));
        return root;
    }
    private String postOrder(TreeNode root){
        if(root==null)return "";
        return postOrder(root.left)+postOrder(root.right)+root.value;
    }
}
```
- 寻找中序遍历的下一个节点
##### 四、面向对象
- java中重写equal为什么要先重写hashcode？如果判断相等，首先判断hashcode是否相等，hashcode相等的前提下，equal才有可能相等。hashcode相等是equal的必要非充分条件
- 接口和抽象类的区别</br>
抽象类可以有成员函数</br>
抽象类可以部分实现</br>
抽象类不可多重继承，但是接口可以
- 实现接口的时候，阅读接口的文档，按照接口合约规范实现接口
- 子类只可以增加父类的功能或者权限，但是不能减少</br>
private ---> public   √</br>
public  ---> private ×</br>
can not reduce visibility
- 泛型的java实现：java Type Erase，也就是，java的泛型在运行的时候，是会被擦除掉的，为了兼容之前的版本，也就是运行时：List,List<String>,List<Integer>没有区别
- 尽量只使用private和public
- 设计模式：Decorator pattern，Abstract factory pattern，Builder Pattern</br>
```java
//Builder Pattern
employee = Employee.fromEnistring(oldEmplyee).withSalary(1500).builder
```
##### 五、高级部分
- 外部排序（例如10G的数据，内存不够了，就要使用外部排序）</br>
扩展的归并排序，分为多段（k路归并，技术点：堆实现，添加缓存）
- 死锁
防止死锁：</br>
- - 破除互斥等待：一般情况下无法破解
- - 破除hold and wait：一次性获取所有的资源
- - 破除循环等待：按顺序获取资源
- - 破除无法剥夺的等待：加入超时
