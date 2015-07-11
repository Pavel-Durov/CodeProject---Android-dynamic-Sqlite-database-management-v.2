package net.simplydone.gui;

import java.util.List;

import net.simplydone.db.DB_BASIC;
import net.simplydone.db.TEST;
import net.simplydone.dynamicsqlite.R;
import net.simplydone.main.DyncamicSqliteApplication;
import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity{

	String LOG_TAG = getClass().getSimpleName();

	DyncamicSqliteApplication _application;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		_application = (DyncamicSqliteApplication)getApplication();
	}
	
	public void OnButtonClick(View sender)
	{
		  int id = sender.getId();
		  
		  switch(id)
		  {
		  	case R.id.bAddNewtoDB:
		  	{
		  		AddNewObjectToDB();
		  		break;
		  	}
		  	case R.id.bGetAllDBdata:
		  	{
		  		GetAllData();
		  		break;
		  	}
		  	case R.id.bUpdate:
		  	{
		  		UpdateRow();
		  		break;
		  	}
		  	case R.id.bDeleteTable:
		  	{
		  		DeleteTable();
		  		break;
		  	}
		  	default:break;
		  }
	}

	private void DeleteTable() 
	{
		int affected = _application.GetDB().DeleteTable(TEST.class);
		ShowMessage(String.format("%d records were deleted", affected));
	}

	private void UpdateRow() 
	{
		TEST t = GenerateTestobj();
		t.ID = 1l;
		int updatedrows = _application.GetDB().UpdateRow(t);
		ShowMessage(String.format("%d records were updated", updatedrows));
	}

	private void GetAllData() 
	{
		List<DB_BASIC> list = ((DyncamicSqliteApplication)getApplication()).GetDB().GetTableData(TEST.class);		
		
		StringBuilder sb = new StringBuilder();
		
		if(list != null)
		{
			for (DB_BASIC object : list) 
			{
				if(object != null && object instanceof TEST)
				{
					TEST test = (TEST)object;
					if(test.ID != null)
					{
						sb.append("ID: " + test.ID + "\n");		
					}
					if(test.TEST_DOUBLE_FIELD_0 != null)
					{
						sb.append("TEST_DOUBLE_FIELD_0" + test.TEST_DOUBLE_FIELD_0 + "\n");		
					}
					if(test.TEST_LONG_FIELD_1 != null)
					{
						sb.append("TEST_LONG_FIELD_1: " + test.TEST_LONG_FIELD_1 + "\n");		
					}
					if(test.TEST_INTEGER_FIELD_2 != null)
					{
						sb.append("TEST_INTEGER_FIELD_2: " + test.TEST_INTEGER_FIELD_2 + "\n");		
					}
					
					if(test.TEST_STRING_FIELD_3 != null)
					{
						sb.append("TEST_STRING_FIELD_3: " + test.TEST_STRING_FIELD_3 + "\n");		
					}
				}
			}
		}
		
		ShowMessage(sb.toString());
	}


	private void ShowMessage(String string) 
	{
		Toast toast = Toast.makeText(this, string, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}

	/*Generate TEST Object*/
	private TEST GenerateTestobj() 
	{
		TEST result = new TEST();
		result.TEST_DOUBLE_FIELD_0 = 0.890;
		result.TEST_INTEGER_FIELD_2 = 34;
		result.TEST_STRING_FIELD_3 = "This is string field";
		return result;
	}
	
	private void AddNewObjectToDB() 
	{
		TEST t = GenerateTestobj();
		_application.GetDB().AddNewObject(t);
	}
}
