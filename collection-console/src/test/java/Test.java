import java.io.File;

public class Test {
    public static void main(String[] args) throws Exception {

        File file = new File("/Volumes/KoChiuHD/2025-05-30 守拙园延时素材");
        for(File file1 : file.listFiles()){
            if(!file1.getName().startsWith("stack_") && file1.getName().endsWith(".tif") && !file1.getName().startsWith(".")){
                file1.renameTo(new File(file1.getParent() + "/stack_" + file1.getName()));
            }
        }
    }
}
