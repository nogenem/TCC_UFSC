package br.ufsc.tcc.common.model;

import java.util.ArrayList;

import br.ufsc.tcc.common.util.CommonUtil;

public class Dewey {
	
	private String value;
	private ArrayList<Integer> numbers;
	private int weight;//TODO remover isso?

	// Construtores
	public Dewey(){
		this("");
	}
	
	public Dewey(String value){
		this.value = value;
		this.weight = Integer.MIN_VALUE;
		this.numbers = this.parseValueToNumbers(value);
	}
	
	// Getters e Setters
	public String getValue(){
		//Atualiza a 'cache'
		if(this.value.isEmpty()){
			this.value = this.parseNumbersToValue(this.numbers);
		}
		return this.value;
	}
	
	public int getDeweyWeight(){
		//Atualiza a 'cache'
		if(this.weight == Integer.MIN_VALUE && !this.numbers.isEmpty()){
			int n = this.numbers.size()-1, tmp = 0;
			this.weight = 0;
			for(int i = n; i>=0; i--){
				tmp += (n-i)*100;
				this.weight += this.numbers.get(i) + tmp;
			}
		}
		return this.weight;
	}
	
	public String getCommonPrefix(Dewey other){
		if(other == null) return "";
		
		String str1 = this.getValue(), 
				str2 = other.getValue();
		
		String result = "";
	    int n1 = str1.length(), n2 = str2.length();
	    for (int i=0, j=0; i<n1 && j<n2; i++, j++){
	        if (str1.charAt(i) != str2.charAt(j))
	            break;
	        result += str1.charAt(i);
	    }
	    if(!result.isEmpty()){
	    	int i = result.lastIndexOf('.');
	    	if(i == -1)
	    		result = "";
	    	else if(result.length()-i <= 2)//Ficou pela metade, ex: 01.02.01 e 01.02.02
		    	result = result.substring(0, result.lastIndexOf('.'));
	    }
		return result;
	}
	
	public int getWidth(){
		return this.numbers.size();
	}
	
	public int getHeight(){
		if(this.numbers.isEmpty()) return Integer.MAX_VALUE;
		return Math.abs(this.numbers.get(0));
	}
	
	public int getMaxHeight(){
		if(this.numbers.isEmpty()) return Integer.MAX_VALUE;
		
		int max = Math.abs(this.numbers.get(0)), tmp = 0;
		for(int i = 1; i<this.numbers.size(); i++){
			tmp = Math.abs(this.numbers.get(i));
			if(tmp > max)
				max = tmp;
		}
		return max;
	}
	
	// Demais métodos
	private Dewey add(int n){
		// Se o 'numbers' estiver vazio não se deve adicionar
		// o valor zero, para evitar coisas como: 00.01 ...
		if(!this.numbers.isEmpty() || n != 0){
			this.numbers.add(n);
			//Limpa a 'cache'
			this.value = "";
			this.weight = Integer.MIN_VALUE;
		}
		return this;
	}
	
	public Dewey distanceOf(Dewey other){
		if(other == null) return null;
		
		Dewey diff = new Dewey();
		ArrayList<Integer> d1 = this.numbers,
				d2 = other.numbers,
				d3 = null;
		int min = Math.min(d1.size(), d2.size());
		
		for(int i = 0; i<min; i++){
			diff.add(d1.get(i) - d2.get(i));
		}
		
		// Caso um deles seja maior que o outro, é preciso 
		// salvar o restante dos numeros do maior
		if(d1.size() < d2.size()) d3 = d2;
		else if(d1.size() > d2.size()) d3 = d1;
		
		if(d3 != null){
			// Se a diferença esta vazia, e caso o maior seja o
			// de baixo, então o 1* numero deve ser negativo
			if(diff.numbers.isEmpty() && d3 == d2){
				diff.add(-d3.get(min++));
			}
			for(int i = min; i<d3.size(); i++){
				diff.add(d3.get(i));
			}
		}
		return diff;
	}
	
	//TODO tratar possivel erro do valueOf?
	public ArrayList<Integer> parseValueToNumbers(String value) {
		ArrayList<Integer> numbers = new ArrayList<>();
		if(value.isEmpty()) return numbers;
		
		String[] tmp = value.split("\\.");
		for(String s : tmp){
			numbers.add(Integer.valueOf(s));
		}
		return numbers;
	}
	
	public String parseNumbersToValue(ArrayList<Integer> numbers){
		if(numbers == null || numbers.isEmpty()) return "";
		
		String value = "";
		for(int n : numbers){
			value += CommonUtil.padNumber(n) + ".";
		}
		value = value.substring(0, value.length()-1);
		return value;
	}
	
	public boolean isNegative(){
		if(this.numbers.isEmpty())
			return false;
		return this.numbers.get(0) < 0;
	}
	
	@Override
	public String toString() {
		return this.getValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Dewey other = (Dewey) obj;
		return this.getValue().equals(other.getValue());
	}
	
	@Override
	public int hashCode() {
		return this.getDeweyWeight();
	}
}
