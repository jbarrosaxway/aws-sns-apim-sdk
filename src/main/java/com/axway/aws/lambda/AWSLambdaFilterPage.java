package com.axway.aws.lambda;

import org.eclipse.swt.widgets.Composite;

import com.vordel.client.manager.wizard.VordelPage;

public class AWSLambdaFilterPage extends VordelPage {

	public AWSLambdaFilterPage() {
		super("AWSLambdaPage");


		setTitle(resolve("AWS_LAMBDA_PAGE"));
		setDescription(resolve("AWS_LAMBDA_PAGE_DESCRIPTION"));
		setPageComplete(true);
	}

	public String getHelpID() {
		 return "com.vordel.rcp.policystudio.filter.help.send_to_s3_bucket_filter_help";
	}

	public boolean performFinish() {
		return true;
	}

	public void createControl(Composite parent) {
		Composite panel = render(parent, getClass().getResourceAsStream("aws_lambda.xml"));
		setControl(panel);
		setPageComplete(true);
	}
}