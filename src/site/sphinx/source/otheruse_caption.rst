--------------------------------------------------------
シート上の見出しの取得
--------------------------------------------------------


読み込み時、書き込み時にマッピングしたセルの見出しを取得することができます。

入力値検証の際などのメッセージの引数に使用したりします。

取得方法は複数ありますが、``Map<String, String> labels`` フィールドを用いるのが記述量が少なく簡単だと思います。
 
.. note:: 
   
   セルの見出しを取得できるのは、アノテーション``@XlsLabelledCell, @XlsColumn, @XlsMapColumns`` を付与したプロパティです。


1. ``Map<String, String> labels`` というフィールドを定義しておくとプロパティ名をキーにセルの位置がセットされるようになっています。
 
   * アノテーション ``@XlsMapColumns`` のセルの位置情報のキーは、 *プロパティ名+['セルの見出し']* としてセットされます。
 
2. アノテーションを付与した *setterメソッド名+Label* というメソッドを用意しておくと、引数にセルの位置が渡されます。
 
   * 位置情報を取得用のsetterメソッドは、引数 ``String label`` を取る必要があります。
   * ただし、``@XlsMapColumns`` に対するsetterメソッドは、第一引数にセルの見出しが必要になります。
   
     * String key, String label
     
3. アノテーションを付与した *フィールド名+Label* というString型のフィールドを用意しておくと、セルの位置が渡されます。
 
   * ただし、``@XlsMapColumns`` に対するフィールドは、``Map<String, String>`` 型にする必要があります。キーには見出しが入ります。

.. sourcecode:: java
	
	@XlsLabelledCell(label="Name")
	public void setName(String name){
		...
	}
	
	public Map<String, String> labels;
	
	// labelsフィールドが定義されている場合は、setter メソッドは必要ありません。
	public void setNameLabel(String label){
		...
	}

.. note:: 
   
   フィールド ``Map<String, String> labels`` と対応するsetterメソッドやフィールドをそれぞれ定義していた場合、
   優先度 *labels > setterメソッド > フィールド* に従い設定されます。

