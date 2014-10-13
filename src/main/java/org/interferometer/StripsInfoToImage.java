package org.interferometer;

import java.awt.Color;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ColorProcessor;
import ij.process.StackProcessor;

import org.interferometer.BordersInfo.PixelType;


public class StripsInfoToImage {
  
    private StripsInfo stripsInfo;
    private String originalFileAbsolutePath;
    
    public StripsInfoToImage(String originalFileAbsolutePath, StripsInfo stripsInfo) {
        if (stripsInfo == null) {
            throw new IllegalArgumentException("stripsInfo cannot be null");
        } 
        if (originalFileAbsolutePath == null) {
            throw new IllegalArgumentException("originalFileAbsolutePath cannot be null");
        } 
        this.stripsInfo = stripsInfo;
        this.originalFileAbsolutePath = originalFileAbsolutePath;
    }
    
    public void write(String fileName) {
        Opener opener = new Opener();
        ImagePlus imp = opener.openImage(originalFileAbsolutePath);
        ColorProcessor ip = (ColorProcessor) imp.getProcessor();
        StackProcessor sp = new StackProcessor(imp.getStack(), ip);
        
        int width = imp.getWidth();
        int height = imp.getHeight();
        
       
        assert(stripsInfo.borders.evaluations.length == height);
        
        for (int i = 0; i < stripsInfo.borders.evaluations.length; i++) {
            assert(stripsInfo.borders.evaluations[i].length == width);
           for (int j = 0; j < stripsInfo.borders.evaluations[i].length; j++) {
               PixelType pixelType = stripsInfo.borders.evaluations[i][j];
                       
                       switch (pixelType) {
                       case Nothing:
                           break;
                       case MayBeMax:
                           ip.setColor(Color.BLACK);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleMax:
                           ip.setColor(Color.GRAY);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleMaxAndBegin:
                           ip.setColor(Color.BLUE);
                           ip.drawPixel(i, j);
                           break;
                       case IThinkMax:
                           ip.setColor(Color.CYAN);
                           ip.drawPixel(i, j);
                           break;
                       case MayBeMin:
                           ip.setColor(Color.WHITE);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleMin:
                           ip.setColor(Color.GREEN);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleMinAndBegin:
                           ip.setColor(Color.MAGENTA);
                           ip.drawPixel(i, j);
                           break;
                       case IThinkMin:
                           ip.setColor(Color.YELLOW);
                           ip.drawPixel(i, j);
                           break;
                       case MayBeNil:
                           ip.setColor(100);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleNil:
                           ip.setColor(110);
                           ip.drawPixel(i, j);
                           break;
                       case PossibleNilAndBegin:
                           ip.setColor(120);
                           ip.drawPixel(i, j);
                           break;
                       case IThinkNil:
                           ip.setColor(130);
                           ip.drawPixel(i, j);
                           break;
                       }        
           }
           
        }
        
        FileSaver fileSaver = new FileSaver(imp);
        fileSaver.saveAsBmp(fileName); 
    }
    
}
