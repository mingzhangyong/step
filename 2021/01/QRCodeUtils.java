package cn.com.egova.sanitation.tools;

import cn.com.im.baselibrary.json.JsonUtil;
import com.swetake.util.Qrcode;
import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;
import jp.sourceforge.qrcode.exception.DecodingFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;


/**
 * Created by hzh on 2017/5/16.
 */

class EQRImage implements QRCodeImage {
    BufferedImage image;

    public EQRImage(BufferedImage image) {
        this.image = image;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getPixel(int x, int y) {
        return image.getRGB(x, y);
    }
}

public class QRCodeUtils {
    private static final Logger logger = LoggerFactory.getLogger(QRCodeUtils.class);

    /**
     * 执行编码，内嵌图片（40）
     * @param content 编码内容
     * @param size 二维码尺寸
     * @param inImg 内嵌图片地址
     * @param dstImage 输出图片地址
     * @throws Exception
     */
    public static void qrCodeEncode(String content, int size, String dstImage, String inImg) throws Exception {
        BufferedImage bufImg = qRCodeCommon(content, size, inImg);
        File dstFile = new File(dstImage);
        // 生成二维码QRCode图片
        ImageIO.write(bufImg, "png", dstFile);
    }

    /**
     * 二维码编码，不内嵌图片
     * @param content 编码内容
     * @param size 二维码尺寸
     * @param dstImage 编码后图片路径
     * @throws Exception
     */
    public static void qrCodeEncode(String content, int size, String dstImage) throws Exception {
        BufferedImage bufImg = qRCodeCommon(content, size, "");
        File dstFile = new File(dstImage);
        // 生成二维码QRCode图片
        ImageIO.write(bufImg, "png", dstFile);
    }

    /**
     * 二维码编码，不内嵌图片
     * @param content 编码内容
     * @param size 二维码内容长度
     * @param dstImage 编码后图片路径
     * @throws Exception
     */
    public static BufferedImage qrCodeEncode2(String content, int size, String dstImage) {
        try {
            BufferedImage bufImg = qRCodeCommon(content, size, "");
            File dstFile = new File(dstImage);
            // 生成二维码QRCode图片
            ImageIO.write(bufImg, "png", dstFile);
            return bufImg;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 二维码编码，以文件流方式
     * @param content 编码内容
     * @param output 输出编码后的图片流
     * @param size 二维码尺寸
     */
    public static void qrCodeEncode(String content, OutputStream output, int size) {
        try {
            BufferedImage bufImg = qRCodeCommon(content, size,"");
            // 生成二维码QRCode图片
            ImageIO.write(bufImg, "png", output);
        } catch (Exception e) {
            logger.error("以文件流方式二维码编码异常,content={}，output={},size={}", new Object[]{content, JsonUtil.toJson(output), size}, e);
        }
    }

    /**
     *
     * @param content 编码内容
     * @param size 二维码内容长度
     * @param innerImage  二维码内嵌图片地址
     * @return
     */
    private static BufferedImage qRCodeCommon(String content, int size, String innerImage) {
        BufferedImage bufImg = null;
        try {
            Qrcode qrcodeHandler = new Qrcode();
            // 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
            qrcodeHandler.setQrcodeErrorCorrect('M');
            qrcodeHandler.setQrcodeEncodeMode('B');
            // 二维码等级 ； 越高存的数据越大，二维码越复杂；设置为0，可任意长度不会报错
            qrcodeHandler.setQrcodeVersion(0);
            // 获得内容的字节数组，设置编码格式
            byte[] contentBytes = content.getBytes("utf-8");
            boolean[][] codeOut = null;
            int imgSize ;
            if (contentBytes.length > 0 && contentBytes.length < 1800) {
                codeOut = qrcodeHandler.calQrcode(contentBytes);
                //根据需要横向绘制的次数来计算二维码的宽度；  12  是下面画笔的width为12
                imgSize = 12 * (codeOut.length + 1);
            }else{
                throw new Exception("QRCode content bytes length = " + contentBytes.length + " not in [0, 800].");
            }


            // 图片尺寸

            bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D gs = bufImg.createGraphics();
            // 设置背景颜色
            gs.setBackground(Color.WHITE);
            gs.clearRect(0, 0, imgSize, imgSize);

            // 设定图像颜色> BLACK
            gs.setColor(Color.BLACK);
            // 设置偏移量，不设置可能导致解析出错
            int pixoff = 8;
            // 输出内容> 二维码

                for (int i = 0; i < codeOut.length; i++) {
                    for (int j = 0; j < codeOut.length; j++) {
                        if (codeOut[j][i]) {
                            gs.fillRect(j * 12 + pixoff, i * 12 + pixoff, 12, 12);
                        }
                    }
                }


            if(innerImage != null && innerImage.length()!=0) {
               // BufferedImage img = ImageIO.read(new File(innerImage));//实例化一个Image对象。
                URL url = new URL(innerImage);
                InputStream is = url.openConnection().getInputStream();
                BufferedImage img = ImageIO.read(is);
                gs.drawImage(img, (imgSize - img.getWidth()) / 2, (imgSize - img.getHeight()) / 2, null);
            }

            gs.dispose();
            bufImg.flush();
        } catch (Exception e) {
            logger.error("生成二维码异常,content={}，innerImage={},size={}", new Object[]{content, innerImage, size}, e);
        }
        return bufImg;
    }

    /**
     * 解析二维码，返回解析内容
     *
     * @param imageFile
     * @return
     */
    public static String qrCodeDecode(File imageFile) {
        String decodedData = null;
        QRCodeDecoder decoder = new QRCodeDecoder();
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
        } catch (IOException e) {
            logger.error("解析二维码异常,imageFile={}",JsonUtil.toJson(imageFile), e);
        }

        try {
            decodedData = new String(decoder.decode(new EQRImage(image)), "gb2313");
            logger.info("Output Decoded Data is：decodedData={}", decodedData);
        } catch (IOException e) {
            logger.error("解析二维码异常,imageFile={}",JsonUtil.toJson(imageFile), e);
        } catch (DecodingFailedException dfe) {
            logger.error("解析二维码异常,imageFile={}",JsonUtil.toJson(imageFile), dfe);
        }
        return decodedData;
    }
}
