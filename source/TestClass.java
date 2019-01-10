
public class TestClass implements TestInterface {
    
    public int testM1(int a, String s, Object o) {
        TestInterface ti = (TestInterface)this;
        ti.testM2(a, s,o);
        return 0;
    }
    
    public String testM2(int a, String s, Object c) {
        return "";
    }
    
    public static void testM3() {
        StringBuffer sb = new StringBuffer();
        sb.append("ciao");
        
    }
    
    static {
        System.out.println();
    }

}
