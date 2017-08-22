import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.transfer.Copy;
import com.amazonaws.services.s3.transfer.Download;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.MultipleFileUpload;
import com.amazonaws.services.s3.transfer.PauseResult;
import com.amazonaws.services.s3.transfer.PersistableDownload;
import com.amazonaws.services.s3.transfer.PersistableTransfer;
import com.amazonaws.services.s3.transfer.PersistableUpload;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferProgress;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.util.StringUtils;

public class AWS4Test {
	
	private static S3 utils =  new S3();
	AmazonS3 svc = utils.getAWS4CLI();
	String prefix = utils.getPrefix();
	static Properties prop = new Properties();
	
	@AfterMethod
	public  void tearDownAfterClass() throws Exception {
		
		utils.tearDown(svc);	
	}

	@BeforeMethod
	public void setUp() throws Exception {
	}
	
	@Test
	public void testObjectCreateBadamzDateAfterEndAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "99990707T215304Z";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadDateAfterEndAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Tue, 07 Jul 9999 21:53:04 GMT";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadamzDateBeforeEpochAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "9500707T215304Z";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadDateBeforeEpochAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Tue, 07 Jul 1950 21:53:04 GMT";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
	}
	
	@Test
	public void testObjectCreateBadAmzDateAfterTodayAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "20300707T215304Z";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadDateAfterToday4AWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Tue, 07 Jul 2030 21:53:04 GMT";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		
	}
	
	@Test
	public void testObjectCreateBadAmzDateBeforeTodayAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "20100707T215304Z";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadDateBeforeToday4AWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Tue, 07 Jul 2010 21:53:04 GMT";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		
	}
	
	@Test
	public void testObjectCreateBadAmzDateNoneAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadDateNoneAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		
	}
	
	@Test
	public void testObjectCreateBadamzDateUnreadableAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "\\x07";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadDateUnreadableAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "\\x07";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "RequestTimeTooSkewed");
		}
	}
	
	@Test
	public void testObjectCreateBadamzDateEmptyAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadDateEmptyAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		
	}
	
	@Test
	public void testObjectCreateBadamzDateInvalidAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Bad date";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("X-Amz-Date", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadDateInvalidAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "Bad date";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Date", value);
		
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		
	}
	
	@Test
	public void testObjectCreateBadUANoneAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("User-Agent", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadUAUnreadableAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "\\x07";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("User-Agent", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadUAEmptyAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("User-Agent", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "SignatureDoesNotMatch");
		}
	}
	
	@Test
	public void testObjectCreateBadAuthorizationInvalidAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "AWS4-HMAC-SHA256 Credential=HAHAHA";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Authorization", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "400 Bad Request");
		}
	}
	
	@Test
	public void testObjectCreateBadAuthorizationIncorrectAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "AWS4-HMAC-SHA256 Credential=HAHAHA";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Authorization", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "400 Bad Request");
		}
	}
	
	@Test
	public void testObjectCreateBadMd5InvalidGarbageAWS4() {
		
		String bucket_name = utils.getBucketName();
		String key = "key1";
		String content = "echo lima golf";
		String value = "AWS4 HAHAHA";
		
		svc.createBucket(new CreateBucketRequest(bucket_name));

		byte[] contentBytes = content.getBytes(StringUtils.UTF8);
		InputStream is = new ByteArrayInputStream(contentBytes);
		
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(contentBytes.length);
		metadata.setHeader("Content-MD5", value);
		
		try {
		svc.putObject(new PutObjectRequest(bucket_name, key, is, metadata));
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "InvalidDigest");
		}
	}
	
	
	@Test
	public void testMultipartUploadMultipleSizesLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		
		CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, 5 * 1024 * 1024, filePath);
		svc.completeMultipartUpload(resp);
		
		CompleteMultipartUploadRequest resp2 = utils.multipartUploadLLAPI(svc, bucket_name, key, 5 * 1024 * 1024 + 100 * 1024, filePath);
		svc.completeMultipartUpload(resp2);
		
		CompleteMultipartUploadRequest resp3 = utils.multipartUploadLLAPI(svc, bucket_name, key, 5 * 1024 * 1024 + 600 * 1024, filePath);
		svc.completeMultipartUpload(resp3);
		
		CompleteMultipartUploadRequest resp4 = utils.multipartUploadLLAPI(svc, bucket_name, key, 10 * 1024 * 1024 + 100 * 1024, filePath);
		svc.completeMultipartUpload(resp4);
		
		CompleteMultipartUploadRequest resp5 = utils.multipartUploadLLAPI(svc, bucket_name, key, 10 * 1024 * 1024 + 600 * 1024, filePath);
		svc.completeMultipartUpload(resp5);
		
		CompleteMultipartUploadRequest resp6 = utils.multipartUploadLLAPI(svc, bucket_name, key, 10 * 1024 * 1024, filePath);
		svc.completeMultipartUpload(resp6);
	}
	
	@Test
	public void testMultipartUploadEmptyLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 0;
		
		try {
			
			CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, size, filePath);
			svc.completeMultipartUpload(resp);
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "XAmzContentSHA256Mismatch");
		}
		
	}
	
	@Test
	public void testMultipartUploadSmallLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 5242880;
			
		CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, size, filePath);
		svc.completeMultipartUpload(resp);
		
	}
	
	@Test
	public void testMultipartUploadIncorrectMissingPartLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 5242880;
			
		List<PartETag> partETags = new ArrayList<PartETag>();

		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket_name, key);
		InitiateMultipartUploadResult initResponse = svc.initiateMultipartUpload(initRequest);

		File file = new File(filePath);
		long contentLength = file.length();
		long partSize = size;
		
		long filePosition = 0;
		for (int i = 1; filePosition < contentLength; i++) {
		    	
		   partSize = Math.min(partSize, (contentLength - filePosition));
		   UploadPartRequest uploadRequest = new UploadPartRequest()
				   .withBucketName(bucket_name).withKey(key)
		           .withUploadId(initResponse.getUploadId()).withPartNumber(i)
		           .withFileOffset(filePosition)
		           .withFile(file)
		           .withPartSize(partSize)
		           ;
		   svc.uploadPart(uploadRequest).setPartNumber(9999);
		   partETags.add((PartETag) svc.uploadPart(uploadRequest).getPartETag());

		   filePosition += partSize;
		}
		
		CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket_name, key, 
                initResponse.getUploadId(), 
                (List<com.amazonaws.services.s3.model.PartETag>) partETags);

		try {
			
			svc.completeMultipartUpload(compRequest);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "InvalidPart");
		}
		
	}
	
	
	@Test
	public void testAbortMultipartUploadNotFoundLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 5242880;
		
		try {
			
			svc.abortMultipartUpload( new AbortMultipartUploadRequest(bucket_name, key, "1"));
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchUpload");
		}
		
	}
	
	@Test
	public void testAbortMultipartUploadLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 5242880;
		
		CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, 5 * 1024 * 1024, filePath);
		svc.abortMultipartUpload( new AbortMultipartUploadRequest(bucket_name, key, resp.getUploadId()));
		
	}
	
	@Test
	public void testMultipartUploadOverwriteExistingObjectLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		long size = 5242880;
		
		svc.putObject(bucket_name, key, "foo");
		
		CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, size, filePath);
		svc.completeMultipartUpload(resp);
		
		Assert.assertNotEquals(svc.getObjectAsString(bucket_name, key), "foo");
		
	}
	
	@Test
	public void testMultipartUploadFileTooSmallFileLLAPI() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/sample.txt";
		long size = 5242880;
			
		try {
			
			CompleteMultipartUploadRequest resp = utils.multipartUploadLLAPI(svc, bucket_name, key, size, filePath);
			svc.completeMultipartUpload(resp);
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "EntityTooSmall");
		}
		
	}
	
	@Test
	public void testMultipartCopyMultipleSizesLLAPI() {
		
		String src_bkt = utils.getBucketName(prefix);
		String dst_bkt = utils.getBucketName(prefix);
		String dir = "./data";
		String key = "key1";
		
		svc.createBucket(new CreateBucketRequest(src_bkt));
		svc.createBucket(new CreateBucketRequest(dst_bkt));
		
		String filePath = "./data/file.mpg";
		Upload upl = utils.UploadFileHLAPI(svc, src_bkt, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		
		CompleteMultipartUploadRequest resp = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 5 * 1024 * 1024);
		svc.completeMultipartUpload(resp);
		
		CompleteMultipartUploadRequest resp2 = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 5 * 1024 * 1024 + 100 * 1024);
		svc.completeMultipartUpload(resp2);
		
		CompleteMultipartUploadRequest resp3 = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 5 * 1024 * 1024 + 600 * 1024);
		svc.completeMultipartUpload(resp3);
		
		CompleteMultipartUploadRequest resp4 = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 10 * 1024 * 1024 + 100 * 1024);
		svc.completeMultipartUpload(resp4);
		
		CompleteMultipartUploadRequest resp5 = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 10 * 1024 * 1024 + 600 * 1024);
		svc.completeMultipartUpload(resp5);
		
		CompleteMultipartUploadRequest resp6 = utils.multipartCopyLLAPI(svc, dst_bkt, key, src_bkt, key, 10 * 1024 * 1024);
		svc.completeMultipartUpload(resp6);
		
	}
	
	
	@Test
	public void testUploadFileHLAPIBigFile() {
	
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String filePath = "./data/file.mpg";
		
		Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
		
		Assert.assertEquals(upl.isDone(), true);
		
	}
	
	@Test
	public void testUploadFileHLAPISmallFile() {
		
		try {
			
			String bucket_name = utils.getBucketName(prefix);
			String key = "key1";
			svc.createBucket(new CreateBucketRequest(bucket_name));
			
			String filePath = "./data/sample.txt";
			
			Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
			
		}catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "400 Bad Request");
		}
		
	}
	
	@Test
	public void testUploadFileHLAPINonExistantBucket() {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		
		String filePath = "./data/sample.txt";
		
		try {
			Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
		
	}
	
	@Test
	public void testMultipartUploadHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
	
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String dir = "./data";
		
		Transfer upl = utils.multipartUploadHLAPI(svc, bucket_name, null, dir);
		
		Assert.assertEquals(upl.isDone(), true);
		
	}
	
	@Test
	public void testMultipartUploadHLAPINonEXistantBucketAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		
		String dir = "./data";
		
		try {
			
			Transfer upl = utils.multipartUploadHLAPI(svc, bucket_name, null, dir);
			
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
		
	}
	
	@Test
	public void testMultipartUploadWithPauseAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException, IOException {
		
		String bucket_name = utils.getBucketName(prefix);
		
		svc.createBucket(new CreateBucketRequest(bucket_name));
		
		String dir = "./data/file.mpg";
		String key = "key1";
		
		TransferManager tm = new TransferManager(svc);
		Upload myUpload = tm.upload(bucket_name, key, new File(dir));
		
		//pause upload
		long MB = 10;
		TransferProgress progress = myUpload.getProgress();
		while( progress.getBytesTransferred() < MB ) Thread.sleep(2000);
		boolean forceCancel = true;
		PauseResult<PersistableUpload> pauseResult = myUpload.tryPause(forceCancel);
		Assert.assertEquals(pauseResult.getPauseStatus().isPaused(), true);
		
		//persist PersistableUpload info to a file
		PersistableUpload persistableUpload = pauseResult.getInfoToResume();
		File f = new File("resume-upload");
		if( !f.exists() ) f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		persistableUpload.serialize(fos);
		fos.close(); 
		
		// Resume upload
		FileInputStream fis = new FileInputStream(new File("resume-upload"));
		PersistableUpload persistableUpload1 = PersistableTransfer.deserializeFrom(fis);
		tm.resumeUpload(persistableUpload1);
		fis.close();
		
	}
	
	@Test
	public void testMultipartCopyHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String src_bkt = utils.getBucketName(prefix);
		String dst_bkt = utils.getBucketName(prefix);
		String dir = "./data";
		String key = "key1";
		
		svc.createBucket(new CreateBucketRequest(src_bkt));
		svc.createBucket(new CreateBucketRequest(dst_bkt));
		
		String filePath = "./data/sample.txt";
		Upload upl = utils.UploadFileHLAPI(svc, src_bkt, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		Copy cpy = utils.multipartCopyHLAPI(svc, dst_bkt, key, src_bkt, key );
		Assert.assertEquals(cpy.isDone(), true);
	}
	
	@Test
	public void testMultipartCopyNoDSTBucketHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String src_bkt = utils.getBucketName(prefix);
		String dst_bkt = utils.getBucketName(prefix);
		String dir = "./data";
		String key = "key1";
		
		svc.createBucket(new CreateBucketRequest(src_bkt));
		
		String filePath = "./data/sample.txt";
		Upload upl = utils.UploadFileHLAPI(svc, src_bkt, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		try {
			
			Copy cpy = utils.multipartCopyHLAPI(svc, dst_bkt, key, src_bkt, key );
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
		
	}
	
	@Test
	public void testMultipartCopyNoSRCBucketHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String src_bkt = utils.getBucketName(prefix);
		String dst_bkt = utils.getBucketName(prefix);
		String dir = "./data";
		String key = "key1";
		
		svc.createBucket(new CreateBucketRequest(dst_bkt));
		
		try {
			
			Copy cpy = utils.multipartCopyHLAPI(svc, dst_bkt, key, src_bkt, key );
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "404 Not Found");
		}
	}
	
	@Test
	public void testMultipartCopyNoSRCKeyHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String src_bkt = utils.getBucketName(prefix);
		String dst_bkt = utils.getBucketName(prefix);
		String dir = "./data";
		String key = "key1";
		
		svc.createBucket(new CreateBucketRequest(src_bkt));
		svc.createBucket(new CreateBucketRequest(dst_bkt));
		
		try {
			
			Copy cpy = utils.multipartCopyHLAPI(svc, dst_bkt, key, src_bkt, key );
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "404 Not Found");
		}
	}
	
	@Test
	public void testDownloadHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		
		String filePath = "./data/sample.txt";
		Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		Download download = utils.downloadHLAPI(svc, bucket_name, key, new File(filePath));
		Assert.assertEquals(download.isDone(), true);
		
	}
	
	@Test
	public void testDownloadNoBucketHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		String key = "key1";
		String filePath = "./data/sample.txt";
		
		try {
			
			Download download = utils.downloadHLAPI(svc, bucket_name, key, new File(filePath));
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "404 Not Found");
		}
		
	}
	
	@Test
	public void testDownloadNoKeyHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		
		String filePath = "./data/sample.txt";
		
		try {
			
			Download download = utils.downloadHLAPI(svc, bucket_name, key, new File(filePath));
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "404 Not Found");
		}
		
	}
	
	@Test
	public void testMultipartDownloadHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		String dstDir = "./downloads";
		
		String filePath = "./data/file.mpg";
		Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		MultipleFileDownload download = utils.multipartDownloadHLAPI(svc, bucket_name, key, new File(dstDir));
		Assert.assertEquals(download.isDone(), true);
	}
	
	@Test
	public void testDownloadWithPauseHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException, IOException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		String filePath = "./data/file.mpg";
		
		TransferManager tm = new TransferManager(svc);
		
		Upload upl = utils.UploadFileHLAPI(svc, bucket_name, key, filePath );
		Assert.assertEquals(upl.isDone(), true);
		
		Download myDownload = tm.download(bucket_name, key, new File(filePath));

		long MB = 20;
		TransferProgress progress = myDownload.getProgress();
		while( progress.getBytesTransferred() < MB ) Thread.sleep(2000);

		// Pause the download and create file to store download info
		PersistableDownload persistableDownload = myDownload.pause();
		File f = new File("resume-download");
		if( !f.exists() ) f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		persistableDownload.serialize(fos);
		fos.close();
		
		//resume download
		FileInputStream fis = new FileInputStream(new File("resume-download"));
		PersistableDownload persistDownload = PersistableTransfer.deserializeFrom(fis);
		tm.resumeDownload(persistDownload);

		fis.close();
		
		
	}
	
	@Test
	public void testMultipartDownloadNoBucketHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		
		String key = "key1";
		String dstDir = "./downloads";
		
		try {
			
			MultipleFileDownload download = utils.multipartDownloadHLAPI(svc, bucket_name, key, new File(dstDir));
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
	}
	
	@Test
	public void testMultipartDownloadNoKeyHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		String dstDir = "./downloads";
		
		
		try {
			
			MultipleFileDownload download = utils.multipartDownloadHLAPI(svc, bucket_name, key, new File(dstDir));
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "404 Not Found");
		}
	}
	
	@Test
	public void testUploadFileListHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		String dstDir = "./downloads";
		
		MultipleFileUpload upl= utils.UploadFileListHLAPI(svc, bucket_name, key);
		Assert.assertEquals(upl.isDone(), true);
		
		ObjectListing listing = svc.listObjects( bucket_name);
		List<S3ObjectSummary> summaries = listing.getObjectSummaries();
		while (listing.isTruncated()) {
		   listing = svc.listNextBatchOfObjects (listing);
		   summaries.addAll (listing.getObjectSummaries());
		}
		Assert.assertEquals(summaries.size(), 2);
		
	}
	
	@Test
	public void testUploadFileListNoBucketHLAPIAWS4() throws AmazonServiceException, AmazonClientException, InterruptedException {
		
		String bucket_name = utils.getBucketName(prefix);
		svc.createBucket(new CreateBucketRequest(bucket_name));
		String key = "key1";
		String dstDir = "./downloads";
		
		try {
			
			MultipleFileUpload upl= utils.UploadFileListHLAPI(svc, bucket_name, key);
		} catch (AmazonServiceException err) {
			AssertJUnit.assertEquals(err.getErrorCode(), "NoSuchBucket");
		}
		
	}
		
		
		
	
}
