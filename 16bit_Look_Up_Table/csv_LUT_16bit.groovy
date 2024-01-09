//http://chenglab.io dan vanselow
//credit for method goes to https://forum.image.sc/t/16bit-lut-for-label-masks/10425/19, 
//https://forum.image.sc/u/petebankhead/

import ij.IJ
import ij.gui.ImageRoi
import ij.gui.Overlay
import ij.io.OpenDialog

import java.awt.image.BufferedImage
import java.awt.image.IndexColorModel
import java.io.BufferedReader
import java.io.FileReader

// Function to convert hex color to RGB bytes
def hexToRgb(String colorStr) {
    colorStr = colorStr.padRight(6, '0')
    int r = Integer.valueOf(colorStr.substring(0, 2), 16)
    int g = Integer.valueOf(colorStr.substring(2, 4), 16)
    int b = Integer.valueOf(colorStr.substring(4, 6), 16)
    return [(byte)r, (byte)g, (byte)b]
}

// Function to read hex color values from CSV file
def readCSV(filePath) {
    def r = new byte[65535]
    def g = new byte[65535]
    def b = new byte[65535]
    def a = new byte[65535]
    Arrays.fill(a, (byte) 255) // Default alpha value

    Arrays.fill(r, (byte) 0)
    Arrays.fill(g, (byte) 0)
    Arrays.fill(b, (byte) 0)

    BufferedReader reader = new BufferedReader(new FileReader(filePath))
    String line = reader.readLine() // Skip header line
    while ((line = reader.readLine()) != null) {
        def tokens = line.split(",")
        int index = Integer.parseInt(tokens[0].trim())
        def (red, green, blue) = hexToRgb(tokens[1].trim())
        r[index] = red
        g[index] = green
        b[index] = blue



    }
    reader.close()
	
    return [r, g, b]
}

// Prompt user to select CSV file
OpenDialog od = new OpenDialog("Select CSV File", "")
String csvFilePath = od.getPath()
if (csvFilePath == null) {
    IJ.error("No file was selected.")
    return
}

// Read hex color values from the selected CSV file
def (r, g, b) = readCSV(csvFilePath)
println("R: ${r}, G: ${g}, B: ${b}")
int n = Math.pow(2, 16)-1 as int
def a = new byte[n]
// Adjustments for IndexColorModel to include alpha channel and transparency handling
Arrays.fill(a, (byte)255)
a[0] = 0
def model = new IndexColorModel(16, n, r, g, b, a)


// Create 16-bit ImageRois, and add to an overlay
def imp = IJ.getImage()
int width = imp.getWidth()
int height = imp.getHeight()
def overlay = new Overlay()
for (int s = 1; s <= imp.getStack().getSize(); s++) {
    def pixels = imp.getStack().getPixels(s) as short[]
    def raster = model.createCompatibleWritableRaster(width, height)
    def buffer = raster.getDataBuffer()
    System.arraycopy(pixels, 0, buffer.getData(), 0, buffer.getData().length);
    def img = new BufferedImage(model, raster, false, null)
    println(img)
    println(raster)

    // Try to show the image in ImageJ with a translucent overlay
    def roi = new ImageRoi(0, 0, img)
    roi.setOpacity(1.0)
    roi.setPosition(s)
    overlay.add(roi)
}
imp.setOverlay(overlay)