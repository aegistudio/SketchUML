package de.dubs.dollarn;
/*
 *  The $N Multistroke Recognizer (Java version)
 *
 *      Jan Sonnenberg, Ph.D.
 *      TU Braunschweig
 *      Institut fuer Nachrichtentechnik
 *      Schleinitzstr. 22
 *      38106 Braunschweig
 *      sonnenberg@ifn.ing.tu-bs.de
 *      
 * Based on the $N Multistroke Recognizer (C# version)
 *
 *	    Lisa Anthony, Ph.D.
 *		UMBC
 *		Information Systems Department
 * 		1000 Hilltop Circle
 *		Baltimore, MD 21250
 * 		lanthony@umbc.edu
 * 
 *      Jacob O. Wobbrock, Ph.D.
 * 		The Information School
 *		University of Washington
 *		Mary Gates Hall, Box 352840
 *		Seattle, WA 98195-2840
 *		wobbrock@u.washington.edu
 *
 * The Protractor enhancement was published by Yang Li and programmed
 * here by Lisa Anthony and Jacob O. Wobbrock.
 *
 *	Li, Y. (2010). Protractor: A fast and accurate gesture 
 *	  recognizer. Proceedings of the ACM Conference on Human 
 *	  Factors in Computing Systems (CHI '10). Atlanta, Georgia
 *	  (April 10-15, 2010). New York: ACM Press, pp. 2169-2172.
 *
 * This software is distributed under the "New BSD License" agreement:
 * 
 * Copyright (c) 2007-2011, Lisa Anthony and Jacob O. Wobbrock
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the University of Washington nor UMBC,
 *      nor the names of its contributors may be used to endorse or promote 
 *      products derived from this software without specific prior written
 *      permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Jacob O. Wobbrock OR Lisa Anthony 
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// This class stores all samples in a Dictionary (which is Hashtable-like)
// data structure for easier processing of a huge corpus when all files
// are read in at once for batch testing (so you don't have to manually
// test each user independently). It also provides easy accessor methods
// for common corpus questions like number of users, categories, etc.

public class SamplesCollection extends
		Hashtable<String, Dictionary<String, Category>> {

	private static final long serialVersionUID = 1L;

	public SamplesCollection() {
		super();
	}

	public Enumeration<String> getUsers() {
		return this.keys();

	}

	public Vector<String> GetUsersVector() {
		Vector<String> allUsers = new Vector<String>(this.size());
		Enumeration<String> keys = keys();
		while (keys.hasMoreElements()) {
			String u = (String) keys.nextElement();
			allUsers.add(u);
		}
		return allUsers;
	}

	public boolean ContainsUser(String user) {
		return this.get(user) != null;
	}

	public Category GetCategoryByUser(String user, String catName) {
		// returns null if user and/or catname are not valid
		if (get(user) == null) {
			return null;
		} else if (get(user).get(catName) == null) {
			return null;
		} else
			return get(user).get(catName);
	}

	public boolean RemoveSamples(String user, String catName) {
		// System.out.println("remove "+user+" "+catName);
		// returns false if user and/or catname are not valid
		if (get(user) == null) {
			return false;
		} else if (get(user).get(catName) == null) {
			return false;
		} else {
			get(user).remove(catName);
			return true;
		}
	}

	public void AddExample(Multistroke p) {
		String catName = Category.ParseName(p.Name);
		// System.out.println("add "+p.User+" "+catName);
		if (get(p.User) != null) {
			// if this user is in the collection, and has some samples in this
			// category already...
			if (get(p.User).get(catName) != null) {
				Dictionary<String, Category> forUser = get(p.User);
				Category cat = (Category) forUser.get(catName);
				cat.AddExample(p); // if the category has been made before, just
									// add to it
			} else // create new category
			{
				Dictionary<String, Category> forUser = get(p.User);
				forUser.put(catName, new Category(catName, p));
			}
		} else // create new user
		{
			Dictionary<String, Category> forUser = new Hashtable<String, Category>();
			forUser.put(catName, new Category(catName, p));
			this.put(p.User, forUser);
		}
	}

	// what is the minimum number of examples per category for this user?
	public int GetMinNumExamplesForUser(String user) {
		int minNumExamples = 9999;
		if (containsKey(user)) {
			Dictionary<String, Category> allCats = get(user);
			Enumeration<Category> en = allCats.elements();
			while (en.hasMoreElements()) {
				Category c = (Category) en.nextElement();
				if (c.getNumExamples() < minNumExamples)
					minNumExamples = c.getNumExamples();
			}
		}
		return minNumExamples;
	}

	// does this user have the same number of samples per category across all
	// categories?
	public boolean AreNumExamplesEqualForUser(String user) {
		if (containsKey(user)) {
			Dictionary<String, Category> allCats = get(user);
			Enumeration<Category> en = allCats.elements();
			int prevNumExamples = -1;
			while (en.hasMoreElements()) {
				Category c = (Category) en.nextElement();
				if (prevNumExamples == -1)
					prevNumExamples = c.getNumExamples();
				if (c.getNumExamples() != prevNumExamples)
					return false;
			}
		}
		return true;
	}

	public int MaxNumCategories() // across all users
	{
		return GetCategoriesVector().size();
	}

	public int NumCategoriesForUser(String user) {
		return get(user).size();
	}

	public Vector<Category> GetCategoriesVector() {
		Vector<Category> allCats = new Vector<Category>();
		Enumeration<String> users = keys();
		while (users.hasMoreElements()) {
			String user = (String) users.nextElement();
			Enumeration<Category> cats = get(user).elements();
			while (cats.hasMoreElements()) {
				Category category = (Category) cats.nextElement();
				if (!allCats.contains(category))
					allCats.add(category);
			}
		}
		return allCats;
	}

	public Vector<Category> GetCategories(String user) {
		Vector<Category> allCats = new Vector<Category>();
		Enumeration<Category> cats = get(user).elements();
		while (cats.hasMoreElements()) {
			Category category = (Category) cats.nextElement();
			if (!allCats.contains(category))
				allCats.add(category);
		}
		return allCats;
	}

}
