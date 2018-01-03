package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;


public class SentNum
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new SentNum(false, true);
  }
  return ref;
}

public static Integer getValue(Annotation np, Document doc)
{
  return (Integer) getInstance().getValueProp(np, doc);
}

private SentNum(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  // Get the sentence annotations
  AnnotationSet sent = doc.getAnnotationSet(Constants.SENT);
  AnnotationSet nps = doc.getAnnotationSet(Constants.NP);
  for (Annotation s : sent) {
    int num = Integer.parseInt(s.getAttribute("sentNum"));
    AnnotationSet enclosed = nps.getOverlapping(s);
    for (Annotation e : enclosed) {
      e.setProperty(this, num);
    }
  }
  if (np.getProperty(this) == null) {
    AnnotationSet ov = sent.getOverlapping(np);
    if (ov == null || ov.size() < 1) throw new RuntimeException("Sentnum not found for " + doc.getAnnotText(np));
    int num = Integer.parseInt(ov.getFirst().getAttribute("sentNum"));
    np.setProperty(this, num);
  }
  return np.getProperty(this);
}

}
