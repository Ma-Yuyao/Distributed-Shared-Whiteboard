package pers.yuyaoma.whiteboard_client;

import java.awt.Color;
import java.awt.Graphics;

/**
 * @author: Yuyao Ma
 * @className: Shape
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Shape class of the whiteboard app, used to store the objects repainted
 * @data: 2021-06-03
 **/

public class Shape 
{
	private int x1, y1, x2, y2;
	private String name, text;
	private Color color;

	/**
	 * Initialize the graphic data when creating a graphic object
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param name
	 * @param color
	 */
	public Shape(int x1, int y1, int x2, int y2, String name, Color color) 
	{
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.name = name;
		this.color = color;
	}
	
	public Shape(int x1, int y1, String text, String name, Color color) 
	{
		this.x1 = x1;
		this.y1 = y1;
		this.text = text;
		this.name = name;
		this.color = color;
	}

	public void reset() 
	{
		x1 = 0;
		y1 = 0;
		x2 = 0;
		y2 = 0;
		name = "";
		color = null;
	}

	/**
	 * Draw the corresponding shape according to the shape name
	 */
	public void drawShape(Graphics g) 
	{
		g.setColor(color);

		switch (name) 
		{
			case "Straight Line":
				g.drawLine(x1, y1, x2, y2);
				break;
			case "Oval":
				g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制椭圆
				break;
			case "Rectangle":
				g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); // 绘制矩形
				break;
			case "Text":
				g.drawString(text, x1, y1);
				break;
		}
	}
}