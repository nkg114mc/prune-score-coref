package edu.oregonstate.nlp.coref.features.properties;

import edu.oregonstate.nlp.coref.data.Annotation;
import edu.oregonstate.nlp.coref.data.AnnotationSet;
import edu.oregonstate.nlp.coref.data.Document;
import edu.oregonstate.nlp.coref.general.Constants;


public class ParNum
    extends Property {

private static Property ref = null;

public static Property getInstance()
{
  if (ref == null) {
    ref = new ParNum(false, true);
  }
  return ref;
}

public static Integer getValue(Annotation np, Document doc)
{
  return (Integer) getInstance().getValueProp(np, doc);
}

private ParNum(boolean whole, boolean cached) {
  super(whole, cached);
}

@Override
public Object produceValue(Annotation np, Document doc)
{
  // Get the sentence annotations
  AnnotationSet par = doc.getAnnotationSet(Constants.PAR);
  AnnotationSet nps = doc.getAnnotationSet(Constants.NP);

  for (Annotation p : par) {
    int num;
    if (Constants.PAR_NUMS_UNAVAILABLE) {
      num = 0;
    }
    else {
      num = Integer.parseInt(p.getAttribute("parNum"));
      //num = Integer.parseInt(p.getAttribute("ParNum"));
    }
    AnnotationSet enclosed = nps.getContained(p);
    for (Annotation e : enclosed) {
      e.setProperty(this, num);
    }
  }

  // Make sure that all annotations have an associated PARNUM
  for (Annotation n : nps) {
    if (n.getProperty(this) == null) {
      AnnotationSet o = par.getOverlapping(0, n.getEndOffset());
      if (o == null || o.size() < 1) {
        n.setProperty(this, 0);
      }
      else {
        Annotation p = o.getLast();
        int num = Integer.parseInt(p.getAttribute("parNum")); // = 0;
        //int num = Integer.parseInt(p.getAttribute("ParNum")); // = 0;
        n.setProperty(this, num);
      }
    }
  }

  if (np.getProperty(this) == null) {
    AnnotationSet o = par.getOverlapping(0, np.getEndOffset());
    if (o == null || o.size() < 1) {
      np.setProperty(this, 0);
    }
    else {
      Annotation p = o.getLast();
      int num = Integer.parseInt(p.getAttribute("parNum"));
      //int num = Integer.parseInt(p.getAttribute("ParNum"));
      np.setProperty(this, num);
    }
  }

  return np.getProperty(this);
}

}
