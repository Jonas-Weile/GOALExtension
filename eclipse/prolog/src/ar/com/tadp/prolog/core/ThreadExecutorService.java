/*****************************************************************************
 * This file is part of the Prolog Development Tools (ProDT)
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/prodevtools
 * Copyright (C): 2008, Claudio Cancinos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.prolog.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author ccancino
 *
 */
public class ThreadExecutorService {
	private static ThreadExecutorService instance;

	private final ExecutorService executorService;

	private ThreadExecutorService() {
		this.executorService = Executors.newCachedThreadPool();
	}

	public static synchronized ThreadExecutorService getInstance() {
		if (instance == null) {
			instance = new ThreadExecutorService();
		}
		return instance;
	}

	public void execute(final Runnable command) {
		this.executorService.execute(command);
	}

	public void awaitTermination(final int waitResponse, final TimeUnit milliseconds) throws InterruptedException {
		this.executorService.awaitTermination(waitResponse, milliseconds);
	}

}
