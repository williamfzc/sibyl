package cases.java8;

public class HelloWorld {
   // fields
   private final String TAG = "abcd";
   private Integer flagInt = 123;

   // inner class
   private static class HiWorld {
      // inner fields
      private final String innerTag = "abcd";
      private Integer innerInt = 123;

      private void innerA() {
         String ok = "ok";
      }
   }

   // method
   private void a() {
      b();
   }

   protected int b() {
      System.out.println("wuhu!");
   }

   public static void main(String[] args) { 
      System.out.println("Hello, World");
   }
}
