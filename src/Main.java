import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;


public class Main {

	public static int curr_cb;
	public static int width , height;
	public static int pixel[][];
	public static int imageType;
	public static HashMap<String,CodeBookVector> code_book = new HashMap<>();
	public static void main(String[] args)
	{
		JFrame jf = new JFrame("Vector Quantizer");
		jf.setContentPane(new Form().get_form());
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);

	}
	public static ArrayList<Vector> divide_to_vectors(int vec_width,int vec_height,int img_width,int img_height,Vector avg_vec)
	{
		Vector vec = new Vector(vec_width,vec_height);
		ArrayList<Vector> vectors = new ArrayList<>();
		for(int x = 0;x < img_width;x += vec_width)
			for(int y = 0;y < img_height;y += vec_height , vectors.add(vec), vec = new Vector(vec_width,vec_height))
				for(int i = x;i < x + vec_width;i++)
					for(int j = y;j < y + vec_height;j++)
					{
						vec.pixels[i - x][j - y] = pixel[i][j];
						avg_vec.pixels[i - x][j - y] += pixel[i][j];
					}
		return  vectors;
	}
	public static void set_code_book_vectors_average(ArrayList<CodeBookVector> cb_vectors)
	{
		for(CodeBookVector cb_vec : cb_vectors)
			cb_vec.set_avg();
	}
	public static void generate_binary_code(String code, int code_book_vec_num, ArrayList<CodeBookVector> cb_vectors)
	{
		if(code.length() == Math.log(code_book_vec_num) / Math.log(2))
		{
			cb_vectors.get(curr_cb).code = code;
			code_book.put(code,cb_vectors.get(curr_cb));
			curr_cb++;
			return;
		}
		generate_binary_code(code + '0', code_book_vec_num, cb_vectors);
		generate_binary_code(code + '1', code_book_vec_num, cb_vectors);
	}
	public static void write_code_book_vector(FileOutputStream out,String code,double[][] cb_vec)
			throws IOException
	{
		out.write(code.getBytes());
		out.write(' ');
		/**write pixels**/
		for(double[] cb_vec_col : cb_vec)
			for(double pixel : cb_vec_col)
				out.write(ByteBuffer.allocate(4).putInt((int)pixel).array());
		out.write('\n');
	}
	public static void write_code_book(FileOutputStream out,int code_book_size, ArrayList<CodeBookVector> code_book)
			throws IOException
	{
		out.write(ByteBuffer.allocate(4).putInt(code_book_size).array());
		out.write('\n');
		for(CodeBookVector cbv : code_book)
			write_code_book_vector(out,cbv.code,cbv.pixels);
	}
	public static void compress(int vec_width,int vec_height,int code_book_size)
			throws IOException
	{
		ArrayList<Vector> vectors = null;
		ArrayList<CodeBookVector> code_book = new ArrayList<>(),temp = null;
		CodeBookVector avg_vec =  new CodeBookVector(vec_width,vec_height,0),left_cb_vec = null,right_cb_vec = null;
		int vecs_width = width / vec_width, vecs_height = height / vec_height
		  , vecs_img_width = vec_width * vecs_width, vecs_img_height = vec_height * vecs_height;
		boolean not_normalized;
		/** divide pixels over vectors, get average & set it in code book vectors list **/
		vectors = divide_to_vectors(vec_width, vec_height, vecs_img_width, vecs_img_height, avg_vec);
		avg_vec.divide(vectors.size());
		avg_vec.code = "0";
		code_book.add(avg_vec);
		/** get required number of code book vectors **/
		while (code_book.size() != code_book_size)
		{
			/** split **/
			temp = new ArrayList<>();
			for(CodeBookVector cb_vec : code_book)
			{
				right_cb_vec = new CodeBookVector(cb_vec.pixels.length,cb_vec.pixels[0].length);
				left_cb_vec = cb_vec.split(right_cb_vec);
				temp.add(left_cb_vec);
				temp.add(right_cb_vec);
			}
			code_book = temp;
			/** divide vectors into groups to nearest vector **/
			CodeBookVector.distribute_vectors(vectors,code_book,false);
			/** set average for each code book vector **/
			set_code_book_vectors_average(code_book);
		}
		/** normalize code book vectors **/
		not_normalized = true;
		for(int i = 0;i < 100 && not_normalized;i++)
		{
			/** redistribute vectors into groups to nearest vector **/
			not_normalized = CodeBookVector.distribute_vectors(vectors,code_book,true);
			/** update average for each code book vector **/
			set_code_book_vectors_average(code_book);
		}
		/** generate binary code to each code book vector **/
		curr_cb = 0;
		generate_binary_code("",code_book_size,code_book);
		/** save compression **/
		FileOutputStream compressed_img = new FileOutputStream("Compressed Image.txt");
		compressed_img.write(ByteBuffer.allocate(4).putInt(vecs_img_width).array());			/**file									              */
		compressed_img.write(ByteBuffer.allocate(4).putInt(vecs_img_height).array());			/**  CI width.CI height.vec width.vec height.cbvs num */
		compressed_img.write(ByteBuffer.allocate(4).putInt(vec_width).array());					/**  code1.cbv1								 		  */
		compressed_img.write(ByteBuffer.allocate(4).putInt(vec_height).array());				/**  code2.cbv2      ~ ~ ~	        		 		  */
		write_code_book(compressed_img,code_book_size,code_book);								/**	 col1 vecs codes						 		  */
		for(int i = 0;i < vecs_width;i++)														/**  col1 vecs codes ~ ~ ~                   		  */
		{
			for(int j = 0;j < vecs_height;j++)
				compressed_img.write(vectors.get(j + i * vecs_height).cbv.code.getBytes());
			compressed_img.write('\n');
		}
		/*ArrayList<String> compression = new ArrayList<>();
		for (Vector vec: vectors)
			compression.add(vec.cbv.code);
		System.out.println(compression);*/
		//decompress(vec_width, vec_height, vecs_img_width, vecs_img_height,compression,code_book);
	}
	public static void decompress()
	throws IOException
	{
		int[][]vec_pixels;
		byte[] int_bytes = new byte[4], string_bytes;
		HashMap<String,int[][]> code_book = new HashMap<>();
		String code;
		int img_width, img_height, vec_width, vec_height, code_book_size, code_size;
		FileInputStream compressed_img = new FileInputStream("Compressed Image.txt");
		compressed_img.read(int_bytes);img_width = ByteBuffer.wrap(int_bytes).getInt();
		compressed_img.read(int_bytes);img_height = ByteBuffer.wrap(int_bytes).getInt();
		compressed_img.read(int_bytes);vec_width = ByteBuffer.wrap(int_bytes).getInt();
		compressed_img.read(int_bytes);vec_height = ByteBuffer.wrap(int_bytes).getInt();
		compressed_img.read(int_bytes);code_book_size = ByteBuffer.wrap(int_bytes).getInt();

		width = img_width;
		height = img_height;
		imageType = 5; ///RGB Color Model
		pixel = new int[img_width][img_height];
		code_size = (int)(Math.log(code_book_size)/Math.log(2));
		/** pass new line **/
		compressed_img.read();
		/** fill code book **/
		string_bytes = new byte[code_size];
		for(int i = 0; i < code_book_size;i++)
		{
			vec_pixels = new int[vec_width][vec_height];
			compressed_img.read(string_bytes);
			code = new String(string_bytes);
			compressed_img.read();/** pass space **/
			for(int j = 0;j < vec_width;j++)
				for(int k = 0;k < vec_height;k++)
				{
					compressed_img.read(int_bytes);
					vec_pixels[j][k] = ByteBuffer.wrap(int_bytes).getInt();
				}
			compressed_img.read();/** pass new line **/
			code_book.put(code,vec_pixels);
		}
		for(int x = 0,idx = 0;x < img_width;x += vec_width)
		{
			for(int y = 0;y < img_height;y += vec_height ,idx++)
			{
				compressed_img.read(string_bytes);
				code = new String(string_bytes);
				for (int i = x; i < x + vec_width; i++)
					for (int j = y; j < y + vec_height; j++)
						pixel[i][j] = code_book.get(code)[i - x][j - y];
			}
			compressed_img.read();/** pass new line **/
	 	}
	}
	public static void readImage_grayScale( String loadPath ) {
		try {
			BufferedImage image;
			File input 	= new File( loadPath );
			
			image 		= ImageIO.read(input);
			width 		= image.getWidth();
			height 		= image.getHeight();
			imageType 	= image.getType();
		
			pixel = new int[width][height];
			for ( int i = 0; i < width; ++i ) {
				for ( int j = 0; j < height; ++j ) {
					Color pixelColor = new Color( image.getRGB(i, j) );
					
					int red 	= (int)( 0.299 * pixelColor.getRed() 	);
					int green 	= (int)( 0.587 * pixelColor.getGreen() 	);
					int blue 	= (int)( 0.114 * pixelColor.getBlue() 	);	
					pixel[i][j] = red + green + blue;
				}
			}
		}
		catch (Exception e) {
			System.out.println( "Image can't be readed" );
		}
	}
	
	public static void saveImage_grayScale( String savePath , String imageName , String imageFormat ) {
		File output = new File( savePath + "\\" + imageName + "." + imageFormat );
		BufferedImage image = new BufferedImage( width , height, imageType );
		
		for ( int i = 0; i < width; ++i ) {
			for ( int j = 0; j < height; ++j ) {
				int grayValue = pixel[i][j];
				Color pixelColor = new Color( grayValue , grayValue , grayValue );
				image.setRGB( i , j , pixelColor.getRGB() );
			}
		}
		
		try {
			ImageIO.write( image , imageFormat , output );
		} catch (IOException e) {
			System.out.println( "Error in save the image" );
		}
	}
}
