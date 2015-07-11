package net.simplydone.db;

import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.simplydone.utils.DateUtils;
import net.simplydone.utils.ReflectionUtils;
import net.simplydone.utils.StringUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 *This class uses java class reflection for dynamic SQlite managment
 * */
public class DataBaseHandler extends SQLiteOpenHelper
{
	//TAG (Class name) for Log tagging
	String LOG_TAG = getClass().getSimpleName();
	
	/**
	 * Initialize databaser with its name and version
	 * @param context
	 * 		Application context
	 * */
	public DataBaseHandler(Context context) 
	{
		super (
				  context, 
				  ConstantsCollection.DATABASE_NAME, 
				  null, 
				  ConstantsCollection.DATABASE_VERSION
			  );
	}
	@Override
	
	public void onCreate(SQLiteDatabase db) {/*DO NOTHING*/}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		onCreate(db);
	}
	/**
	 *Creates table using reflection of given object,
	 *each member of given object will be converted to a row in a database table.
	 *if used primitive convertible to Sqlite format types.
	 * 
	 * @param object
	 * 		an object on which reflection will be based the table creation
	 * */
	public void CreateTable(DB_BASIC object) 
	{
		if(object != null)
		{
			SQLiteDatabase db = null;
			try 
			{
				db = getWritableDatabase();
			
				Class<? extends DB_BASIC> c = object.getClass();
				String tableName = c.getName();
				
				tableName = ReflectionUtils.GetClassName(c);
				
				//Fields of the object
				Field[] fields = c.getFields();
				
				StringBuilder sbCreateTable  = new StringBuilder();
				
				//Beginning of the CREATE raw query
				sbCreateTable.append(ConstantsCollection.SQLITE_CREATE_TABLE_IF_NOT_EXISTS);
				sbCreateTable.append(tableName);
				sbCreateTable.append(ConstantsCollection.SQLITE_OPENNING_BRACKET);
				
				//Iterates on the given object fields using reflection 
				//and creates appropriate column definition
				for (int i = 0; i < fields.length; i++) 
				{
					String  fieldName= fields[i].getName();
					
					if(fieldName.equalsIgnoreCase(ConstantsCollection.ID))
					{//Creates an auto increament index named ID
						sbCreateTable.append(fieldName);
						sbCreateTable.append(ConstantsCollection.SQLITE_INTEGER_PRIMARY_KEY_AUTOINCREMENT);
					}
					else
					{//Creates column declaration
						String rowname = GetSqliteType(fields[i].getType());
						
						if(rowname != null)
						{
							sbCreateTable.append(fieldName);
							sbCreateTable.append(ConstantsCollection.SQLITE_SPACE);
							sbCreateTable.append(rowname);
						}
					}
					
					if(i != fields.length - 1)
					{//Allways adds , in the end of each column declaration except the last one
						sbCreateTable.append(ConstantsCollection.SQLITE_COMMA);
						sbCreateTable.append(ConstantsCollection.SQLITE_SPACE);
					}
				}
			
				//Closing raw CREATE Query with }; characters
				sbCreateTable.append(ConstantsCollection.SQLITE_CLOSING_BRACKET);
				sbCreateTable.append(ConstantsCollection.SQLITE_CLOSING_SEMICOLUMN);
				
				//Executes raw SQlite statement
				db.execSQL(sbCreateTable.toString());
			}
			catch (SecurityException e) 
			{
				Log.e(LOG_TAG, e.toString());
			} 
			catch (Exception e) 
			{
				Log.e(LOG_TAG, e.toString());
			} 
			finally 
			{
				//Closing the DB connection
				CloseDB(db);
			}
		}
	}
	/**
	 * Finds appropriate Sqlite raw string class to given java class
	 * @return Sqlite row format 
	 * */
	private String GetSqliteType(Class<?> c) 
	{
		String type = "TEXT";
		
		if (c.equals(String.class)) 
		{
			type = ConstantsCollection.SQLITE_TEXT;
		} 
		else if (  c.equals(Integer.class) 
				|| c.equals(Long.class) 
				|| c.equals(Number.class) 
				|| c.equals(java.util.Date.class)) 
		{
			type = ConstantsCollection.SQLITE_INTEGER;
		}
		else if(c.equals(Double.class))
		{
			type = ConstantsCollection.SQLITE_DOUBLE;
		}
		return type;
	}
	/**
	 * Adds given object to the database, by its class name. Perform INSERT Sqlite operation
	 * @param object to be inserted
	 * @return id of the inserted object
	 * */
	public long AddNewObject(DB_BASIC object)
	{		
		long result = ConstantsCollection.INDEX_NOT_DEFINED;
		if(object != null)
		{
			SQLiteDatabase db = null;
			try 
			{
				db = this.getWritableDatabase();
		
				ContentValues values = new ContentValues();
				Class<? extends DB_BASIC> c = object.getClass();
				Field[] fields = c.getFields();
				
				//Iterates on object's members
				for (Field field : fields) 
				{
					Object val = GetValue(field, object);
					
					if (val != null) 
					{
						String rawValue = null;
						if (field.getType().equals(Date.class)) 
						{
							try 
							{
								rawValue = DateUtils.DateToValue((Date) val);
							} 
							catch (ParseException e) 
							{
								Log.e(LOG_TAG, e.toString());
							}
						} 
						else 
						{
							rawValue = val.toString();
						}
						
						String name = field.getName();
		
						values.put(name, rawValue); 
					}
				}
		
				String tableName = ReflectionUtils.GetClassName(object.getClass());
				
				if(values.size() > 0)
				{
					result = db.insert(tableName, null, values);
				}
			} 
			finally 
			{
				CloseDB(db);
			}
		}
		
		return result;
	}
	/**
	 * Gets the value of the fields in specified object using reflection
	 * 
	 * @return the value of the field
	 * */
	private Object GetValue(Field field, DB_BASIC object) 
	{
		Object result = null;
		try 
		{
			result = field.get(object);
		} 
		catch (IllegalAccessException e1) 
		{
			Log.e(LOG_TAG, e1.toString());
		} 
		catch (IllegalArgumentException e1) 
		{
			Log.e(LOG_TAG, e1.toString());
		}
		return result;
	}
	/**
	 * Deletes the table from database
	 * @param tblClass
	 * 		table object
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise.
	 * */
	public  int DeleteTable(Class<? extends DB_BASIC> tblClass) 
	{
		int result = -1;
		SQLiteDatabase db = null;
		try 
		{
			db = this.getWritableDatabase();
			String tblName = ReflectionUtils.GetClassName(tblClass);
			result = db.delete(tblName, null, null);
		} 
		finally 
		{
			CloseDB(db);
		}
		
		return result;
	}
	/**
	 * Converts cursor to List of objects
	 * @param cursor
	 * 			database cursor
	 * @param clazz
	 * 		the desired clazz
	 * 
	 * @return converted cursor object to List collection
	 **/
	@SuppressWarnings("unchecked")
	private <T> List<T> ConvertCursorToObjects(Cursor cursor, Class<? extends DB_BASIC> clazz) 
	{
		List<T> list = new ArrayList<T>();
		
		//moves the cursor to the first row
		if (cursor.moveToFirst()) 
		{
			String[] ColumnNames = cursor.getColumnNames();
			do 
			{
				Object obj = ReflectionUtils.GetInstance(clazz);
				
				//iterates on column names
				for (int i = 0; i < ColumnNames.length; i++) 
				{
					try {
						
						Field field = obj.getClass().getField(ColumnNames[i]);
						Object objectValue = null;
						String str = cursor.getString(i);
						
						if(str != null)
						{
							//Converting stored Sqlite data to java objects
							if (field.getType().equals(java.util.Date.class)) 
							{
								Date date = DateUtils.ValueToDate(str);
								objectValue = date;
								field.set(obj, objectValue);
							} 
							else if (field.getType().equals(Number.class))
							{
								objectValue = NumberFormat.getInstance().parse(str);
							}
							else if(field.getType().equals(Long.class) )
							{
								objectValue = NumberFormat.getInstance().parse(str);
								long value = Long.parseLong(objectValue.toString());
								field.set(obj, value);
							}
							else if(field.getType().equals(Integer.class) )
							{
								objectValue = NumberFormat.getInstance().parse(str);
								int value = Integer.parseInt(str);
								field.set(obj, value);
							}
							else if(field.getType().equals(Double.class) )
							{
								objectValue = NumberFormat.getInstance().parse(str);
								double value = Double.parseDouble(objectValue.toString());
								field.set(obj, value);
							}
							else 
							{
								objectValue = str;
								field.set(obj, objectValue);
							}
						}
					}
					catch (IllegalArgumentException e) 
					{
						Log.e(LOG_TAG, e.toString());
					} 
					catch (IllegalAccessException e) 
					{
						Log.e(LOG_TAG, e.toString());
					} 
					catch (ParseException e) 
					{
						Log.e(LOG_TAG, e.toString());
					}
					catch (SecurityException e) 
					{
						Log.e(LOG_TAG, e.toString());
					} 
					catch (NoSuchFieldException e) 
					{
						Log.e(LOG_TAG, e.toString());
					} 
				}
				
				if(obj  instanceof DB_BASIC)
				{
					list.add((T) obj);
				}
			} while (cursor.moveToNext());
		}
		return list;
	}
	/**
	 * Gets all data from specified table by class instance
	 * @return null if no objects are located, List<DB_BASIC> there is records
	 * */
	public List<DB_BASIC> GetTableData(Class<? extends DB_BASIC> clazz) 
	{		
		List<DB_BASIC> list;
		String tableName = ReflectionUtils.GetClassName(clazz);
	
		SQLiteDatabase db = null;
		try 
		{
			db = this.getWritableDatabase();
			String[] columns = null;
			
			Cursor cursor = db.query(tableName, columns, null,null, null, null, null);
					
			list = ConvertCursorToObjects(cursor, clazz);
		} 
		finally 
		{
			CloseDB(db);
		}
		return list;
	}
	/**
	 * Updates row in relevant table, specified by class instance
	 * @return number of updated rows or -1 on fail
	 * */
	public int UpdateRow(DB_BASIC object) 
	{ 
		int result = ConstantsCollection.INDEX_NOT_DEFINED;
		
		if(object != null && object.ID != null)
		{
			SQLiteDatabase db = null;
			
			db = this.getWritableDatabase();
			String tableName = ReflectionUtils.GetClassName(object.getClass());
			
			StringBuilder sbWhereClause = new StringBuilder();
			
			sbWhereClause.append(ConstantsCollection.SQLITE_SPACE);
			sbWhereClause.append(ConstantsCollection.ID);
			sbWhereClause.append(ConstantsCollection.SQLITE_EQUAL_SIGN);
			sbWhereClause.append(String.valueOf(object.ID)); 
			
			ContentValues values = new ContentValues();
			
			//iterates on fields
			for(Field f : object.getClass().getFields())
			{
				String fieldValue = GetStringValue(f, object);
				String name = f.getName();
				
				if( fieldValue != null 
					&& !fieldValue.equals(StringUtils.EMPTY_STRING)
					&& !name.equals(ConstantsCollection.ID) )
				{
					values.put(name, fieldValue);
				} 
			}
			result = db.update(tableName, values, sbWhereClause.toString(), null);
		}
	
		return result;
	}
	/**
	 * @return String value of the object
	 * */
	private String GetStringValue(Field field, DB_BASIC object)
	{
		Class<?> type = field.getType();
		String result = null;
		
		Object value = GetValue(field, object);
	
		if (value != null) 
		{
				if (type.equals(Date.class)) 
				{
					try 
					{
						result = DateUtils.DateToValue((Date) value);
					}
					catch (ParseException e) 
					{
						e.printStackTrace();
					}
				} 
				else 
				{
					result = value.toString();
				}
		}
		
		return result;
	}
	/**
	 * Closes database connection
	 * @param db 
	 * 		database reference
	 * */
	private void CloseDB(SQLiteDatabase db) 
	{
		try 
		{
			if (db != null) 
			{
				db.close();
			}
		} 
		catch (Exception e) 
		{
			Log.e(LOG_TAG, e.toString());
		}
		
	}
}
