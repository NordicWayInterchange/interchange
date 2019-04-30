package no.vegvesen.ixn.federation.dbhelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFiller {

	@Autowired
	private DatabaseHelperInterface dbHelper;

	public static void main(String[] args){

		DatabaseFiller d = new DatabaseFiller();

		d.dbHelper.fillDatabase();
	}
}
