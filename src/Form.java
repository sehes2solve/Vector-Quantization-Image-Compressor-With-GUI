import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Form
{

    private JPanel jpanel1;
    private JButton compression_btn;
    private JTextField vec_width_txt;
    private JTextField vec_height_txt;
    private JTextField cb_size_txt;
    private JTextField path_txt;
    private JButton comp_btn;
    private JButton dec_btn;
    private JLabel img_path_lbl;
    private JLabel vec_width_lbl;
    private JLabel vec_height_lbl;
    private JLabel cb_size_lbl;

    public JPanel get_form(){ return jpanel1; }

    public Form() {
        compression_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compression_btn.setVisible(false);
                dec_btn.setVisible(false);
                img_path_lbl.setVisible(true);path_txt.setVisible(true);
                vec_width_lbl.setVisible(true);vec_width_txt.setVisible(true);
                vec_height_lbl.setVisible(true);vec_height_txt.setVisible(true);
                cb_size_lbl.setVisible(true);cb_size_txt.setVisible(true);
                comp_btn.setVisible(true);
            }
        });
        comp_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.readImage_grayScale(path_txt.getText());
                try {
                    Main.compress(Integer.parseInt(vec_width_txt.getText()),
                                   Integer.parseInt(vec_height_txt.getText()),
                                   Integer.parseInt(cb_size_txt.getText()));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                comp_btn.setVisible(false);
                img_path_lbl.setVisible(false);path_txt.setVisible(false);
                vec_width_lbl.setVisible(false);vec_width_txt.setVisible(false);
                vec_height_lbl.setVisible(false);vec_height_txt.setVisible(false);
                cb_size_lbl.setVisible(false);cb_size_txt.setVisible(false);
                compression_btn.setVisible(true);
                dec_btn.setVisible(true);
            }
        });
        dec_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Main.decompress();
                    String save_imagePath 		= "C:\\Users\\pc\\Desktop\\Vector Quantization\\";			/** example: C:\Users\Public\Pictures\Sample Pictures\  									**/
                    String save_imageName 		= "After Compression";
                    String save_imageFormat		= "jpg";		/** note write it without dot [ ".jpg" not acceptable ] [ "jpg" is acceptable ]				**/
                    Main.saveImage_grayScale( save_imagePath , save_imageName , save_imageFormat );
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
