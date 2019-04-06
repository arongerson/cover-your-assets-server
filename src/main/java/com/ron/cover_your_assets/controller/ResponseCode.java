package com.ron.cover_your_assets.controller;

public enum ResponseCode {
	
	RCRTGM(1), RJNGM(2), RRCNNCT(3), RSTL(4), RYLD(5),
	RDSCRD(6), RNSTRCTN(7), RCVRDSCRD(8), RCVRWN(9), RGTSCRS(10), 
	RGTWNSCR(11), RSTRT(12), RDRW(13);
	
	private final int number;
	
	private ResponseCode(int number) {
		this.number = number;
	}
	
	public int getNumber() {
        return this.number;
    }
}
