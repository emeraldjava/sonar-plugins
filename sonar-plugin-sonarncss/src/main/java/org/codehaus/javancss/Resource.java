/*
Copyright (C) 2001 Chr. Clemens Lee <clemens@kclee.com>.

This file is part of JavaNCSS

JavaNCSS is free software; you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the
Free Software Foundation; either version 2, or (at your option) any
later version.

JavaNCSS is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License
along with JavaNCSS; see the file COPYING.  If not, write to
the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.  */package org.codehaus.javancss;

import java.util.Set;
import java.util.TreeSet;
import java.util.SortedSet;

public class Resource implements Comparable<Resource> {

	public enum Type {
		PROJECT, FILE, CLASS, METHOD, PACKAGE
	}

	private Type type;

	private String name;

	private Resource parent;

	protected long linesNumber = 0;

	protected long ncss = 0;

	protected long multiComNumber = 0;

	protected long singleComNumber = 0;

	protected long cc = 0;

	protected long methodsNumber = 0;

	protected long classesNumber = 0;
	
	protected long packagesNumber = 0;

	protected long javadocLinesNumber = 0;

	protected int javadocBlocksNumber = 0;

	private SortedSet<Resource> children = new TreeSet<Resource>();

	public Resource(String name, Type type) {
		this.name = name;
		this.type = type;
		if(type.equals(Resource.Type.PACKAGE)){
			packagesNumber++;
		} else if(type.equals(Resource.Type.CLASS)){
			classesNumber++;
		} else if (type.equals(Resource.Type.METHOD)){
			methodsNumber++;
		}
	}

	public void addChild(Resource resource) {
		resource.setParent(this);
		if (!children.contains(resource)) {
			children.add(resource);
		}
	}

	private void setParent(Resource parent) {
		this.parent = parent;
	}

	public int compareTo(Resource resource) {
		return this.name.compareTo(resource.getName());
	}

	public String getName() {
		return name;
	}

	public String getFullName() {
		if (parent != null && !parent.getType().equals(Type.PROJECT)) {
			return new StringBuilder().append(parent.getFullName()).append(".").append(getName()).toString();
		}
    return getName();
	}

	public Type getType() {
		return type;
	}

	public int getNumberOfClasses() {
		return children.size();
	}

	public Resource getParent() {
		return parent;
	}

	public Set<Resource> getChildren() {
		return children;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Resource)) {
			return false;
		}
		Resource resource = (Resource) obj;
    return name.equals(resource.getName()) && type.equals(resource.getType());
  }

	public int hashCode() {
		return name.hashCode() + type.hashCode();
	}

	public long getClassesNumber() {
		return classesNumber;
	}

	public long getMethodsNumber() {
		return methodsNumber;
	}

	public long getSingleComNumber() {
		return singleComNumber;
	}

	public void setMultiComNumber(long multiComNumber) {
		this.multiComNumber = multiComNumber;
	}

	public void setSingleComNumber(long singleComNumber) {
		this.singleComNumber = singleComNumber;
	}

	public long getMultiComNumber() {
		return multiComNumber;
	}

	public void setLinesNumber(long linesNumber) {
		this.linesNumber = linesNumber;
	}

	public long getLinesNumber() {
		return linesNumber;
	}

	public void incrementNcss() {
		ncss++;
	}

	public long getNcss() {
		return ncss;
	}

	public void incrementCc() {
		cc++;
	}

	public long getCc() {
		return cc;
	}

	public long getJavadocBlocksNumber() {
		return javadocBlocksNumber;
	}

	public void setJavadocLinesNumber(long javadocLinesNumber) {
		this.javadocLinesNumber = javadocLinesNumber;
	}

	public long getJavadocLinesNumber() {
		return javadocLinesNumber;
	}

}
