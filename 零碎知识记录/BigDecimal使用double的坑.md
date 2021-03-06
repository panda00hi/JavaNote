在程序开发中，小数的运算要格外注意，并非我们表面认识上的十进制运算，因为我们的CPU表示浮点数由两个部分组成：指数和尾数。计算机底层二进制存储机制再加上开发语言用于外显时做的处理等，导致与认知不符。

小数是近似值


虽然可以使用8位精确地描述0至255之间的所有自然数，但是描述所有0.0至255.0之间的实数则需要无限数量的位。首先，在该范围内（甚至在0.0-0.1范围内）有无数个要描述的数字，其次，某些无理数根本无法用数字描述。例如e和π。换句话说，数字2和0.2在计算机中的显示方式大不相同。


整数由代表值2n的位表示，其中n是位的位置。因此，值6被表示为23 * 0 + 22 * 1 + 21 * 1 + 20 * 0与位序列0110相对应。另一方面，小数由代表2-n（即分数）1/2, 1/4, 1/8,...的位描述，数字0.75对应于2-1 * 1 + 2-2 * 1 + 2-3 * 0 + 2-4 * 0产生位序列1100 (1/2 + 1/4)。


有了这些知识，我们可以制定以下经验法则：任何十进制数字都由一个近似值表示。


让我们通过执行一系列琐碎的乘法来研究其实际后果。


System.out.println( 0.2 + 0.2 + 0.2 + 0.2 + 0.2 );

1.0

打印1.0。尽管这确实是正确的，但它可能给我们一种错误的安全感。巧合的是，0.2是Java能够正确表示的少数几个值之一。让我们通过另一个琐碎的算术问题再次挑战Java，将数字0.1加十倍。


System.out.println( 0.1f + 0.1f + 0.1f + 0.1f + 0.1f + 0.1f + 0.1f + 0.1f + 0.1f + 0.1f );

System.out.println( 0.1d + 0.1d + 0.1d + 0.1d + 0.1d + 0.1d + 0.1d + 0.1d + 0.1d + 0.1d );


1.0000001

0.9999999999999999

根据Joseph D. Darcy博客的幻灯片，这两个计算的总和分别为0.100000001490116119384765625和0.1000000000000000055511151231...。这些结果对于有限的一组数字是正确的。float的精度为8个前导数字，而double的精度为17个前导数字。现在，如果预期结果1.0与屏幕上打印的结果之间的概念上的不匹配不足以使您的警钟响起，那么请注意Mr.的数字。达西的幻灯片似乎与印刷的数字不符！那是另一个陷阱。关于此的更多信息。


在看似最简单的情况下意识到了错误的计算之后，就可以合理地考虑印象可能会出现多快了。让我们将问题简化为仅添加三个数字。


System.out.println( 0.3 == 0.1d + 0.1d + 0.1d );

false

令人震惊的是，不精确性已经在三个附加项中开始发挥作用！


加倍溢出


与Java中的其他任何简单类型一样，双精度数由一组有限的位表示。因此，将一个值加或乘以一个双可以产生令人惊讶的结果。诚然，数字必须很大才能溢出，但是它确实发生了。让我们尝试相乘然后除以大数。数学直觉说结果是原始数字。在Java中，我们可能会得到不同的结果。


double big = 1.0e307 * 2000 / 2000;

System.out.println( big == 1.0e307 );

false

这里的问题是先将big乘以并溢出，然后再将溢出的数相除。更糟糕的是，不会向程序员发出异常或其他类型的警告。基本上，这使表达式x * y完全不可靠，因为在通常情况下，对于由x，y表示的所有double值均不作任何表示或保证。


大大小小的不是朋友！


劳雷尔和哈代经常在很多事情上意见分歧。同样在计算中，大大小小的都不是朋友。使用固定位数表示数字的结果是，在相同的计算中对非常大和非常小的数字进行运算将无法按预期工作。让我们尝试将小的东西添加到大的东西中。


System.out.println( 1234.0d + 1.0e-13d == 1234.0d );

true

加法没有效果！这与任何（理智的）加法直觉相矛盾，即直觉上给定两个数d和f，则d + f> d。


小数不能直接比较


到目前为止，我们所学到的是，我们必须抛弃在数学课和整数编程中获得的所有直觉。请谨慎使用小数。例如，该语句for(double d = 0.1; d != 0.3; d += 0.1)实际上是一个伪装的永无止境的循环！错误是直接将十进制数字相互比较。您应遵循以下准则。


避免两个小数之间的相等性测试。不要在容忍度可以是常量的情况下if(a == b) {..}使用if(Math.abs(a-b) < tolerance) {..}，例如，公共静态最终双容忍度= 0.01请考虑使用运算符<，>作为替代，因为它们可以更自然地描述您想要表达的内容。例如，我更喜欢表单 for(double d = 0; d <= 10.0; d+= 0.1)而不是笨拙的 for(double d = 0; Math.abs(10.0-d) < tolerance; d+= 0.1) 两种表单，但都视情况而定：在单元测试中，我更倾向于表达，assertEquals(2.5, d, tolerance)而assertTrue(d > 2.5)不是说不仅第一个表单读得更好，而且通常是您想要进行的检查正在做（即d不太大）。


所见即所得-所见即所得


所见即所得是通常在图形用户界面应用程序中使用的表达式。它的意思是“所见即所得”，用于计算以描述一个系统，在该系统中，编辑期间显示的内容看起来与最终输出非常相似，该最终输出可能是打印的文档，网页等。这个短语最初是一个流行的流行短语，由Flip Wilson的扮靓人物“ Geraldine”提出，她经常说“您看到的就是您所得到的”，以借口她古怪的行为（来自维基百科）。


程序员经常陷入的另一个严重陷阱，就是认为十进制数是所见即所得。必须意识到，在打印或写入十进制数时，不是打印/写入的近似值。换一种说法，Java在幕后做了很多近似，并且不断地试图使您不了解它。只有一个问题。您需要了解这些近似值，否则您的代码中可能会遇到各种各样的神秘错误。


但是，只要有一些独创性，我们就可以调查幕后的真实情况。到现在为止，我们知道数字0.1已被近似表示。


System.out.println( 0.1d );

0.1

我们知道0.1不是0.1，但屏幕上仍打印了0.1。结论：Java是所见即所得！


为了多样化，让我们选择另一个看起来很纯真的数字，例如2.3。像0.1一样，2.3是一个近似值。毫不奇怪，当打印数字时，Java隐藏了近似值。


System.out.println( 2.3d );

2.3

要研究2.3的内部近似值是多少，我们可以将该数字与附近的其他数字进行比较。


double d1 = 2.2999999999999996d;

double d2 = 2.2999999999999997d;

System.out.println( d1 + " " + (2.3d == d1) );

System.out.println( d2 + " " + (2.3d == d2) );

2.2999999999999994 false

2.3 true

因此2.2999999999999997等于2.3等于2.3！还要注意，由于近似值，枢轴点位于..99997处，而不是..99995处，通常在数学上四舍五入。掌握近似值的另一种方法是调用BigDecimal的服务。


System.out.println( new BigDecimal(2.3d) );

2.29999999999999982236431605997495353221893310546875

现在，不要以为您可以跳船而仅使用BigDecimal来得意。BigDecimal此处记录了自己的陷阱集合。


没有什么是容易的，而且几乎没有免费的东西。而“自然”，浮动和双打则在打印/书写时产生不同的结果。


System.out.println( Float.toString(0.1f) );

System.out.println( Double.toString(0.1f) );

System.out.println( Double.toString(0.1d) );

0.1

0.10000000149011612

0.1

根据约瑟夫·D·达西（Joseph D. Darcy）博客的幻灯片，浮点近似具有24个有效位，而双近似具有53个有效位。士气是为了保留值，必须以相同格式读取和写入十进制数字。


被0除


许多开发人员从经验中知道，将数字除以零会导致应用程序突然终止。在int上进行操作时，发现了类似的行为，但很令人惊讶，在double上进行操作时，却发现了Java。除零外，任何数字除以零分别得出∞或-∞。将零除以零会产生特殊的NaN，即非数字值。


System.out.println(22.0 / 0.0);

System.out.println(-13.0 / 0.0);

System.out.println(0.0 / 0.0);

Infinity

-Infinity

NaN

将正数与负数相除会产生负数的结果，而将负数与负数相除会产生正数的结果。由于可以用零除，因此根据将数字除以0.0还是-0.0会得到不同的结果。对，是真的！Java为负零！但是不要被愚弄，两个零值相等，如下所示。


System.out.println(22.0 / 0.0);

System.out.println(22.0 / -0.0);

System.out.println(0.0 == -0.0);

Infinity

-Infinity

true

无限很奇怪


在数学世界中，无穷大是我很难理解的概念。例如，当一个无穷大比另一个无穷大时，我从未获得直觉。当然，Z> N，所有有理数的集合都比自然数的集合无限大，但这大约是我在这方面的直觉极限！


幸运的是，Java中的无穷与数学世界中的无穷差不多。您可以对无穷大的值执行通常的可疑行为（+，-，*，/，但不能将无穷大应用于无穷大。


double infinity = 1.0 / 0.0;

System.out.println(infinity + 1);

System.out.println(infinity / 1e300);

System.out.println(infinity / infinity);

System.out.println(infinity - infinity);

Infinity

Infinity

NaN

NaN

这里的主要问题是返回NaN值而没有任何警告。因此，如果您愚蠢地调查某个特定的双精度是偶数还是奇数，您确实会陷入困境。也许运行时异常会更合适？


double d = 2.0, d2 = d - 2.0;

System.out.println("even: " + (d % 2 == 0) + " odd: " + (d % 2 == 1));

d = d / d2;

System.out.println("even: " + (d % 2 == 0) + " odd: " + (d % 2 == 1));

even: true odd: false

even: false odd: false

突然，您的变量既不是奇数也不是偶数！NaN甚至比Infinity还要奇怪。无限值与double的最大值不同，而NaN与无限值又不同。


double nan = 0.0 / 0.0, infinity = 1.0 / 0.0;

System.out.println( Double.MAX_VALUE != infinity );

System.out.println( Double.MAX_VALUE != nan );

System.out.println( infinity         != nan );

true

true

true

通常，当双精度数已获取值NaN时，对其进行任何运算都会得到NaN。


System.out.println( nan + 1.0 );

NaN

结论


小数是近似值，而不是您分配的值。在数学世界中获得的任何直觉都不再适用。期望a+b = a和a != a/3 + a/3 + a/3

避免使用==，与一些容差进行比较或使用> =或<=运算符

Java是所见即所得！永远不要相信您打印/写入的值是近似值，因此始终以相同格式读取/写入十进制数。

注意不要使双精度对象溢出，也不要使双精度对象变为±Infinity或NaN。无论哪种情况，您的计算结果都可能不符合您的预期。您可能会发现，始终在返回方法值之前先检查这些值是一个好主意。