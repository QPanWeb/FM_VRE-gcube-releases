package org.gcube.portlets.user.dataminermanager.client.custom.progress;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.sencha.gxt.cell.core.client.ProgressBarCell.ProgressBarAppearance;
import com.sencha.gxt.cell.core.client.ProgressBarCell.ProgressBarAppearanceOptions;
import com.sencha.gxt.core.client.GXT;
import com.sencha.gxt.core.client.XTemplates;
import com.sencha.gxt.core.client.util.Format;

/**
 * 
 * @author Giancarlo Panichi email: <a
 *         href="mailto:g.panichi@isti.cnr.it">g.panichi@isti.cnr.it</a>
 *
 */
public class OrangeProgressBarAppearance implements ProgressBarAppearance {

	public interface OrangeProgressBarResources {

		ImageResource barOrange();

		ImageResource innerBarOrange();

		OrangeProgressBarStyle style();

	}

	public interface OrangeProgressBarStyle extends CssResource {

		String progressBarOrange();

		String progressInnerOrange();

		String progressTextOrange();

		String progressTextBackOrange();

		String progressWrapOrange();

	}

	public interface OrangeProgressBarTemplate extends XTemplates {

		@XTemplate(source = "OrangeProgressBar.html")
		SafeHtml render(SafeHtml text, OrangeProgressBarStyle style,
				SafeStyles wrapStyles, SafeStyles progressBarStyles,
				SafeStyles progressTextStyles, SafeStyles widthStyles);

	}

	public interface OrangeProgressBarDefaultResources extends
			OrangeProgressBarResources, ClientBundle {

		@Source({ "OrangeProgressBar.css" })
		@Override
		OrangeProgressBarStyle style();

		@Source("orange-progress-bg.gif")
		@ImageOptions(repeatStyle = RepeatStyle.Horizontal)
		@Override
		ImageResource barOrange();

		@Source("orange-bg.gif")
		@ImageOptions(repeatStyle = RepeatStyle.Horizontal)
		@Override
		ImageResource innerBarOrange();
	}

	private final OrangeProgressBarStyle style;
	private OrangeProgressBarTemplate template;

	
	public OrangeProgressBarAppearance() {
		this(
				GWT.<OrangeProgressBarDefaultResources> create(OrangeProgressBarDefaultResources.class),
				GWT.<OrangeProgressBarTemplate> create(OrangeProgressBarTemplate.class));
	}

	public OrangeProgressBarAppearance(OrangeProgressBarResources resources,
			OrangeProgressBarTemplate template) {
		this.style = resources.style();
		this.style.ensureInjected();
		this.template = template;
	}

	@Override
	public void render(SafeHtmlBuilder sb, Double value,
			ProgressBarAppearanceOptions options) {
		value = value == null ? 0 : value;
		double valueWidth = value * options.getWidth();

		int vw = new Double(valueWidth).intValue();

		String text = options.getProgressText();

		if (text != null) {
			int v = (int) Math.round(value * 100);
			text = Format.substitute(text, v);
		}

		SafeHtml txt;
		if (text == null) {
			txt = SafeHtmlUtils.fromSafeConstant("&#160;");
		} else {
			txt = SafeHtmlUtils.fromString(text);
		}

		int adj = GXT.isIE() ? 4 : 2;

		SafeStyles wrapStyles = SafeStylesUtils.fromTrustedString("width:"
				+ (options.getWidth() - adj) + "px;");
		SafeStyles progressBarStyles = SafeStylesUtils
				.fromTrustedString("width:" + vw + "px;");
		SafeStyles progressTextStyles = SafeStylesUtils
				.fromTrustedString("width:" + Math.max(vw - 8, 0) + "px;");
		SafeStyles widthStyles = SafeStylesUtils.fromTrustedString("width:"
				+ (Math.max(0, options.getWidth() - adj)) + "px;");
		sb.append(template.render(txt, style, wrapStyles, progressBarStyles,
				progressTextStyles, widthStyles));
	}

}
