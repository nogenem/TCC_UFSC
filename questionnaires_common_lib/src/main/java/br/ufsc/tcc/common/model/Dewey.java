package br.ufsc.tcc.common.model;

import java.util.ArrayList;

import br.ufsc.tcc.common.util.CommonUtil;

public class Dewey {
	
	private String value;
	private ArrayList<Integer> numbers;

	// Construtores
	public Dewey(){
		this("");
	}
	
	public Dewey(String value){
		this.value = value;
		this.numbers = this.parseValueToNumbers(value);
	}
	
	// Getters e Setters
	public String getValue(){
		//Atualiza a 'cache'
		if(this.value.isEmpty())
			this.value = this.parseNumbersToValue(this.numbers);
		return this.value;
	}
	
	public ArrayList<Integer> getNumbers(){
		return this.numbers;
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
	    	else if(result.length()-i <= 3)//Ficou pela metade, ex: 001.002.001 e 001.002.002
		    	result = result.substring(0, result.lastIndexOf('.'));
	    }
		return result;
	}
	
	public int getWidth(){
		return this.numbers.size();
	}
	
	public int getHeight(){
		if(!this.numbers.isEmpty()) 
			return Math.abs(this.numbers.get(0));
		return Integer.MAX_VALUE;
	}
	
	public int getMaxHeight(){
		if(!this.numbers.isEmpty()) {
			int max = Math.abs(this.numbers.get(0)), tmp = 0;
			for(int i = 1; i<this.numbers.size(); i++){
				tmp = Math.abs(this.numbers.get(i));
				if(tmp > max)
					max = tmp;
			}
			return max;
		}
		return Integer.MAX_VALUE;
	}
	
	// Demais métodos
	private Dewey add(int n){
		// Se o 'numbers' estiver vazio não se deve adicionar
		// o valor zero, para evitar coisas como: 00.01 ...
		if(!this.numbers.isEmpty() || n != 0){
			this.numbers.add(n);
			//Limpa a 'cache'
			this.value = "";
		}
		return this;
	}
	
	public Dewey distanceOf(Dewey other){
		if(other == null) return null;
		
		Dewey dist = new Dewey();
		ArrayList<Integer> n1 = this.numbers,
				n2 = other.numbers,
				n3 = null;
		int min = Math.min(n1.size(), n2.size());
		
		for(int i = 0; i<min; i++)
			dist.add(n1.get(i) - n2.get(i));
		
		// Caso um deles seja maior que o outro, é preciso 
		// salvar o restante dos numeros do maior		
		if(n1.size() < n2.size()) n3 = n2;
		else if(n1.size() > n2.size()) n3 = n1;
		
		if(n3 != null){
			// Se a diferença esta vazia, e caso o maior seja o
			// de baixo, então o 1* numero deve ser negativo
			if(dist.numbers.isEmpty() && n3 == n2){
				dist.add(-n3.get(min++));
			}
			for(int i = min; i<n3.size(); i++){
				dist.add(n3.get(i));
			}
		}
		return dist;
	}
	
	//TODO tratar possivel erro do valueOf?
	public ArrayList<Integer> parseValueToNumbers(String value) {
		ArrayList<Integer> numbers = new ArrayList<>();
		if(!value.isEmpty()){
			value = value.trim();
			String[] tmp = value.split("\\.");
			for(String s : tmp){
				numbers.add(Integer.valueOf(s));
			}
		}
		return numbers;
	}
	
	public String parseNumbersToValue(ArrayList<Integer> numbers){
		String value = "";
		if(numbers != null && !numbers.isEmpty()){
			for(int n : numbers){
				value += CommonUtil.padNumber(n) + ".";
			}
			value = value.substring(0, value.length()-1);
		}
		return value;
	}
	
	public boolean isNegative(){
		if(!this.numbers.isEmpty())
			return this.numbers.get(0) < 0;
		return false;
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
		return this.getValue().hashCode();
	}
}
