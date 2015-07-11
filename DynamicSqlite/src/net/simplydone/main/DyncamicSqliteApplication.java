package net.simplydone.main;

import net.simplydone.db.DataBaseHandler;
import net.simplydone.db.TEST;
import android.app.Application;

public class DyncamicSqliteApplication extends Application{

	private DataBaseHandler _dbhandler;
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
	}
	
	public DataBaseHandler GetDB()
	{
		if(_dbhandler == null)
		{
			InitDB();
		}
		
		return _dbhandler;
	}

	private void InitDB() 
	{
		//Initialize DB handler
		_dbhandler = new DataBaseHandler(this);
		//Adding table based on class TEST reflection
		_dbhandler.CreateTable(new TEST());
	}
}
