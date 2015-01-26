package com.example.placesrecommendation;

public class Place {

	String m_name;
	
	String m_Addresse;
	
	
	String m_Offszeiten;
	
	String m_telephoneNummar;
	
	Place(String _name, String _Addresse, String OffZeiten, String telefonNummar)
	{
		m_name = _name;
		
		m_Addresse = _Addresse;
		
		m_Offszeiten = OffZeiten;
		
		m_telephoneNummar = telefonNummar;
	}
	
}
