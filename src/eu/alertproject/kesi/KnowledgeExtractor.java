/*
 * Copyright (C) 2011 GSyC/LibreSoft
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * Authors: Santiago Dueñas <sduenas@libresoft.es>
 *
 */

package eu.alertproject.kesi;

public abstract class KnowledgeExtractor extends Thread {
    private String extractor;
    private JobsQueue queue;

    public KnowledgeExtractor(String extractor, JobsQueue queue) {
        this.extractor = extractor;
        this.queue = queue;
    }

    public void run() {
        while (true) {
            int result;
            Job job;
            ToolRunner tr;

            try {
                job = queue.take();
            } catch (InterruptedException e) {
                System.err.println("Unexpected error in jobs queue. "
                                   + e.getMessage());
                continue;
            }

            String[] cmd = getCommandExtractor(job.getUrl(), job.getType());

            tr = new ToolRunner();
            System.out.println("Processing job " + job.getID());
            result = tr.run(extractor, cmd);
            System.out.println("Job processed. Result: " + result);

            if (result == 0) {
                String data = getExtractedData(job.getUrl(), job.getType(),
                                               job.getID());
                if (data != null) {
                    System.out.println(data);
                } else {
                    System.err.println("Failured extraction");
                }
            } else {
                System.err.println("Failured job");
            }
        }
    }

    public abstract String[] getCommandExtractor(String url, String type);

    public abstract String getExtractedData(String url, String type, String id);

}
