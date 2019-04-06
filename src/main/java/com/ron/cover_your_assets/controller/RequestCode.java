package com.ron.cover_your_assets.controller;

public enum RequestCode {
	
	CRTGM(1), JNGM(2), RCNNCT(3), DWNLD(4), GTCRDS(5),
	STL(6), YLD(7), DSCRD(8), NSTRCTN(9), CVRDSCRD(10), 
	CVRWN(11), GTSCRS(12), GTWNSCR(13), STRT(14), DRW(15);
	
	private final int number;
	
	private RequestCode(final int number) {
		this.number = number;
	}
	
	public final int getNumber() {
        return this.number;
    }
}
