package newView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MenuBackgroundPanel extends JPanel
{
    private BufferedImage image;

    public MenuBackgroundPanel(LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
        this.setupImage();
    }

    public MenuBackgroundPanel(LayoutManager layout)
    {
        super(layout);
        this.setupImage();
    }

    public MenuBackgroundPanel(boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
        this.setupImage();
    }

    public MenuBackgroundPanel()
    {
        this.setupImage();
    }

    private void setupImage()
    {
        try
        {
            image = ImageIO.read(new File("src/images/mainMenu/sanimalBackground.png"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
