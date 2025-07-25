package com.axway.aws.sns;

import org.eclipse.swt.widgets.Composite;

import com.vordel.client.manager.wizard.VordelPage;

public class PublishSNSMessageFilterPage extends VordelPage {

	public PublishSNSMessageFilterPage() {
		super("AWSSNSPage");

		setTitle(resolve("AWS_SNS_PAGE"));
		setDescription(resolve("AWS_SNS_PAGE_DESCRIPTION"));
		setPageComplete(true);
	}

	public String getHelpID() {
		 return "com.vordel.rcp.policystudio.filter.help.send_to_sns_filter_help";
	}

	public boolean performFinish() {
		return true;
	}

	public void createControl(Composite parent) {
		Composite panel = render(parent, getClass().getResourceAsStream("aws_sns.xml"));
		setControl(panel);
		setPageComplete(true);
	}
} 