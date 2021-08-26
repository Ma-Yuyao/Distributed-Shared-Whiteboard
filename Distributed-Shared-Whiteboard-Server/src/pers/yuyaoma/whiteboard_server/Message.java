package pers.yuyaoma.whiteboard_server;

/**
 * @author: Yuyao Ma
 * @className: Message
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Message class used to create the message object used in Server
 * @data: 2021-06-03
 **/

class Message 
{
	//Name of client
	String client;
	//Message
	String message;

	public Message() 
	{
		super();
		// TODO Auto-generated constructor stub
	}

	public Message(String client, String message) 
	{
		super();
		this.client = client;
		this.message = message;
	}

	public String getName() 
	{
		return client;
	}

	public void setName(String name) 
	{
		this.client = name;
	}

	public String getMessage() 
	{
		return message;
	}

	public void setMessage(String message) 
	{
		this.message = message;
	}

	@Override
	public String toString() 
	{
		return (client + message);
	}
}