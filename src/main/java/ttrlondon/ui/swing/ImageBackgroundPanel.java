package ttrlondon.ui.swing;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * Panel that paints an image as a cover-style background behind child components.
 */
final class ImageBackgroundPanel extends JPanel {
  private final Image image;

  ImageBackgroundPanel(Image image) {
    this.image = image;
    setOpaque(image == null);
  }

  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    if (image == null) {
      return;
    }
    int imageWidth = image.getWidth(this);
    int imageHeight = image.getHeight(this);
    if (imageWidth <= 0 || imageHeight <= 0) {
      return;
    }
    double scale = Math.max((double) getWidth() / imageWidth, (double) getHeight() / imageHeight);
    int width = (int) Math.round(imageWidth * scale);
    int height = (int) Math.round(imageHeight * scale);
    int x = (getWidth() - width) / 2;
    int y = (getHeight() - height) / 2;
    graphics.drawImage(image, x, y, width, height, this);
  }
}
