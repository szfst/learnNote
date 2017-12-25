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
- 二分查找法处理边界问题（a+(b-a)/2）,顺便查 看java源码如何处理这个问题（右移一位相当于除以2
<code>int mid = (low + high) >>> 1</code>
）
- 面试树的问题问的比较多，图相对少
- 二叉树的遍历
- 根据前序、遍历中序遍历找出后续遍历（方法一：建树，方法二：不建树）
- 寻找中序遍历的下一个节点
