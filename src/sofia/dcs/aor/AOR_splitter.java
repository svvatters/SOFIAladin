package sofia.dcs.aor;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * @author bwarrington@sofia.usra.edu
 *
 */
public class AOR_splitter {
	private static final int SPLIT_THRESHOLD = 3600;

	public static void main(String[] args) throws JAXBException, IOException {

		// create JAXB context and instantiate marshaller
		JAXBContext context = JAXBContext.newInstance(ObservingPlan.class);
		Unmarshaller um = context.createUnmarshaller();

		// create JAXB context and instantiate marshaller
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		for (int j = 0; j < args.length; j++) {
			System.out.println("Processing " + args[j]);
			ObservingPlan plan = (ObservingPlan) um.unmarshal(new FileReader(
					args[j]));
			ArrayList<Observation> list = plan.getObservations();
			ArrayList<Observation> new_list = new ArrayList<Observation>();
			for (Observation obs : list) {
				int duration = obs.getDurationInSeconds();
				int n = duration / SPLIT_THRESHOLD;
				if (n > 1) {
					System.out.println("\tId: " + obs.getId() + " Duration: "
							+ duration + " will be split into " + n + "parts.");
					String id = obs.getId();
					obs.setDurationInSeconds(duration / n);
					obs.setOverheadInSeconds(obs.getOverheadInSeconds() / n);
					for (int i = 1; i <= n; i++) {
						Observation new_obs = new Observation(obs);
						new_obs.setId(id + "#" + i);
						new_list.add(new_obs);
					}
				} else {
					new_list.add(obs);
				}
			}
			plan.setObservations(new_list);
			// Write to File
			m.marshal(plan, new File(args[j]));
		}
		System.out.println("finished");
	}
}
