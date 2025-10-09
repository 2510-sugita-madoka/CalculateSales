package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 商品定義ファイル名
	private static final String FILE_NAME_COMMODI_LST = "commodity.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// 商品別集計ファイル名
	private static final String FILE_NAME_COMMODI_OUT = "commodity.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";
	private static final String FILE_INVALID_NUMBER = "売上ファイル名が連番になっていません";
	private static final String SALES_AMOUNT_10DIGIT_OVER = "合計金額が10桁を超えました";
	private static final String DATA_INVAL_BRANCH_CODE = "の支店コードが不正です";
	private static final String DATA_BRANCH_CODE_FORMAT = "のフォーマットが不正です";


	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {

		// エラー処理 3-1
		// コマンドライン引数が渡されていない場合エラー
		if (args.length != 1) {
			System.out.println(UNKNOWN_ERROR);
			return;
		}

		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 商品コードと商品名を保持するMap
		Map<String, String> commodiCode = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> commodiSales = new HashMap<>();

		// 売上ファイルを保持するList
		List<File> rcdFiles = new ArrayList<File>();

		// 支店定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// 商品定義 1-3,1-4
		// 商品定義ファイル読み込み処理
		if(!readFile(args[0], FILE_NAME_COMMODI_LST, commodiCode, commodiSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		// 売上ファイルの判定　(処理内容2-1)
		File[] files = new File(args[0]).listFiles();

		for(int i = 0; i < files.length ; i++) {
			// 名称が「数字8桁.rcd」のファイルをリストに保持する（パス）
			if(files[i].getName().matches("^[0-9]{8}.rcd$")) {
				rcdFiles.add(files[i]);
			}
		}

		// エラー処理 2-1
		// 売上ファイルが連番でない場合エラー
		Collections.sort(rcdFiles);
		for(int i = 0; i < rcdFiles.size() -1; i++) {

			//⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し
			//比較した際に差が1以外である場合エラー
			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i + 1).getName().substring(0, 8));

			if((latter - former) != 1) {
				System.out.println(FILE_INVALID_NUMBER);
				return;

			}
		}

		// 集計　(処理内容2-2)
		for(int i = 0; i < rcdFiles.size(); i++) {

			//売上ファイルの中身を読み込む
			BufferedReader br = null;
			try {

				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				String line;

				List<String> branchItem = new ArrayList<String>();

				// Listに入れる（1行目:支店コード/2行目:商品コード/3行目:金額）
				while((line = br.readLine()) != null) {
					branchItem.add(line);
				}

				// エラー処理 2-4
				// 売上ファイルの中身が3行ではなかった場合はエラー
				if(branchItem.size() != 3) {
					System.out.println(rcdFiles.get(i).getName() + DATA_BRANCH_CODE_FORMAT);
					return;
				}

				// エラー処理 2-3
				// 売上ファイルの支店コードが支店定義ファイルに該当しなかった場合はエラー
				if (!branchNames.containsKey(branchItem.get(0))) {
					System.out.println(rcdFiles.get(i).getName() + DATA_INVAL_BRANCH_CODE);
					return;
				}

				// エラー処理 3-2
				// 売上ファイルの売上金額が数字ではなかった場合はエラー
				if(!branchItem.get(2).matches("^[0-9]*$")){
					System.out.println(UNKNOWN_ERROR);
					return;
				}

				// 加算した支店ごとの売上⾦額を計算する
				long fileSale = Long.parseLong(branchItem.get(2));
				Long saleAmount = branchSales.get(branchItem.get(0)) + fileSale;

				// 商品定義 2-2
				// 加算した商品ごとの売上⾦額を計算する
				Long salecommodiAmount = commodiSales.get(branchItem.get(1)) + fileSale;

				// エラー処理 2-2
				// 集計した売上金額が10桁を超えた場合エラー
				if(saleAmount >= 10000000000L){
					System.out.println(SALES_AMOUNT_10DIGIT_OVER);
					return;
				}

				// 計算結果をmapに格納する
				branchSales.put(branchItem.get(0), saleAmount);
				commodiSales.put(branchItem.get(1), salecommodiAmount);


			} catch(IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;
			} finally {
				// ファイルを開いている場合
				if(br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch(IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}
		}


		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

		// 商品定義 3-2
		// 支店別集計ファイル書き込み処理
		if(!writeFile(args[0], FILE_NAME_COMMODI_OUT, commodiCode, commodiSales)) {
			return;
		}
	}


	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名 支店定義ファイル
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		BufferedReader br = null;

		try {

			File file = new File(path, fileName);

			// エラー処理 1-1
			// ⽀店定義ファイルが存在しない場合エラー
			if(!file.exists()) {
				System.out.println(FILE_NOT_EXIST);
				return false;
			}

			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// 一行ずつ読み込む
			while((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				String[] items = line.split(",");

				/* エラー処理 1-2
				   支店コードと支店名が,区切りでない
				   または支店コードが3桁でない場合はエラー*/
				if((fileName == FILE_NAME_BRANCH_LST) && ((items.length != 2) || (!items[0].matches("^[0-9]{3}$")))){
					System.out.println(FILE_INVALID_FORMAT);
					return false;
				}

				//支店名、売上金額をそれぞれmapに保持する
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);
			}

		} catch(IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if(br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}



	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames, Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		// 書き込み処理を行う
		BufferedWriter bw = null;
		File file = new File(path, fileName);

		try {

			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			// branchNames/branchSalesからそれぞれ値を書き出す
			for (String key : branchNames.keySet()) {
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				bw.newLine();
			}

		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			if(bw != null) {
				try {
					bw.close();
				} catch(IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}

		return true;
	}

}
