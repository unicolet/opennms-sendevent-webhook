package osewh;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

/**
 * Utility class (singleton) to return a byte[] representing
 * a 1x1 transparent gif image.
 */
public class Gif {
    private byte[] image;
    private static Gif instance;
    
    private Gif() {
        BufferedImage img = new BufferedImage(1, 1,BufferedImage.TYPE_BYTE_INDEXED);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "GIF", bos);
        image=bos.toByteArray();
        bos.close();
    }
    
    private byte[] getImage() {
        return image;
    }
    
    public static byte[] getImageBytes() {
        if(instance==null) {
            instance=new Gif();
        }
        
        println("Image size: "+instance.getImage().length)
        return instance.getImage();
    }
    
}